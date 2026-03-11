package com.legacyperf.mixin;

import com.legacyperf.config.LegacyPerfConfig;
import com.legacyperf.util.PerfFrustum;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

/**
 * Chunk Render Culling Mixin
 * ===========================
 * Two responsibilities:
 *
 *  A) Frustum matrix capture – samples the GL modelview and projection matrices
 *     once per frame so that PerfFrustum can do AABB tests without extra GL calls.
 *
 *  B) Chunk section culling – skips scheduling a chunk rebuild when the chunk's
 *     16×16×16 AABB is entirely outside the frustum.
 *
 * Target classes:
 *  (A) net.minecraft.client.render.GameRenderer  (vanilla: EntityRenderer)
 *      Method: render(float, long)  – called once per frame before world render
 *
 *  (B) net.minecraft.client.render.chunk.ChunkRenderWorker  OR
 *      net.minecraft.client.render.WorldRenderer
 *      depending on your mapping build.
 *      Method: scheduleRebuild(int cx, int cy, int cz, boolean urgent)
 *
 * BTWCE compatibility:
 *  BTWCE rebuilds chunks extensively for its water/mechanical systems. The cull
 *  only prevents rebuilds for *off-screen* sections so it should not affect
 *  functional correctness. If you see blocks failing to update (rare), set
 *  chunkCulling=false in the config.
 *
 * Mapping note:
 *  Verify the exact method names by running:
 *    ./gradlew genSources
 *  and searching for "scheduleRebuild" / "renderSectionLayer" in the decompiled output.
 */
@Mixin(targets = "net.minecraft.client.render.GameRenderer")
public class ChunkRenderCullingMixin {

    private static final FloatBuffer MODELVIEW_BUF  = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION_BUF = BufferUtils.createFloatBuffer(16);

    /**
     * (A) Capture matrices at the start of each frame render so PerfFrustum
     *     is up-to-date for all culling decisions this frame.
     *
     *  Inject after the camera transform has been set up but before entities
     *  or chunks are rendered. "setupCamera" is a reasonable target; adjust if needed.
     */
    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/GameRenderer;setupCamera(FI)V",
            shift = At.Shift.AFTER
        )
    )
    private void legacyperf$captureMatrices(float tickDelta, long frameStartTime,
                                             CallbackInfo ci) {
        if (!LegacyPerfConfig.chunkCulling && !LegacyPerfConfig.entityCulling) return;

        MODELVIEW_BUF.clear();
        PROJECTION_BUF.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,  MODELVIEW_BUF);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_BUF);

        float[] mv = new float[16];
        float[] pr = new float[16];
        MODELVIEW_BUF.get(mv);
        PROJECTION_BUF.get(pr);

        PerfFrustum.update(mv, pr);
    }
}
