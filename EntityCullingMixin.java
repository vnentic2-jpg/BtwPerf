package com.legacyperf.mixin;

import com.legacyperf.config.LegacyPerfConfig;
import com.legacyperf.util.PerfFrustum;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entity Culling Mixin
 * =====================
 * Injects into EntityRenderDispatcher#render (formerly RenderManager in vanilla).
 *
 * Mapping note (barn / Legacy Fabric 1.6.4):
 *   net.minecraft.client.render.entity.EntityRenderDispatcher
 *   Method: render(Entity, double, double, double, float, float, CallbackInfo)
 *
 * If this class fails to load due to a mapping mismatch, check the exact name
 * with:  ./gradlew remapJar  and inspect the resulting mixin refmap JSON, or
 * look up the obfuscated name in the yarn 1.6.4 mappings spreadsheet.
 *
 * BTWCE compatibility: BTWCE adds custom entities that extend vanilla base
 * classes. The inject point (HEAD, cancellable) fires before any BTWCE entity
 * logic, so it is fully safe; cancelling just skips the render call, not tick.
 */
@Mixin(targets = "net.minecraft.client.render.entity.EntityRenderDispatcher")
public class EntityCullingMixin {

    /**
     * Skip rendering an entity whose AABB is entirely outside the view frustum.
     *
     * Parameters reflect the unmapped 1.6.4 render signature:
     *   render(Entity entity, double x, double y, double z,
     *          float yaw, float tickDelta)
     */
    @Inject(
        method = "render(Lnet/minecraft/entity/Entity;DDDFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void legacyperf$cullEntity(Entity entity,
                                       double x, double y, double z,
                                       float yaw, float tickDelta,
                                       CallbackInfo ci) {

        if (!LegacyPerfConfig.entityCulling) return;

        // Use the entity's bounding box if available; fall back to a 1×2×1 box.
        double halfW, height;
        if (entity.boundingBox != null) {
            halfW  = (entity.boundingBox.maxX - entity.boundingBox.minX) * 0.5;
            height =  entity.boundingBox.maxY - entity.boundingBox.minY;
        } else {
            halfW  = 0.5;
            height = 2.0;
        }

        double minX = x - halfW;
        double minY = y;
        double minZ = z - halfW;
        double maxX = x + halfW;
        double maxY = y + height;
        double maxZ = z + halfW;

        if (PerfFrustum.isAabbOutside(minX, minY, minZ, maxX, maxY, maxZ,
                LegacyPerfConfig.entityCullingPadding)) {
            ci.cancel();
        }
    }
}
