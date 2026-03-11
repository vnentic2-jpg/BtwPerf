package com.legacyperf.mixin;

import com.legacyperf.config.LegacyPerfConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FPS Limiter Mixin
 * ==================
 * Target: net.minecraft.client.MinecraftClient (the main game loop).
 *
 * Injects a sleep at the END of each frame to cap the frame rate.
 * This is intentionally placed after the full frame has been rendered so that
 * vsync and BTWCE's own timing are not disturbed.
 *
 * Only active when LegacyPerfConfig.fpsLimit > 0.
 *
 * BTWCE compatibility: BTWCE modifies the game loop for its own purposes but
 * does not hook the end-of-frame tick in a way that would conflict here.
 * If you observe desynced animations, raise fpsLimit or disable this feature.
 *
 * Mapping note:
 *   net.minecraft.client.MinecraftClient  (vanilla: net.minecraft.client.Minecraft)
 *   Method: render(boolean) – the per-frame render method called from the main loop.
 *   In some mapping versions this is called "tick" or "runGameLoop"; adjust if needed.
 */
@Mixin(targets = "net.minecraft.client.MinecraftClient")
public class FpsLimiterMixin {

    /** Nanosecond timestamp of the last frame. */
    private long legacyperf$lastFrameNs = 0L;

    /**
     * After each frame completes, sleep until the next frame is due.
     */
    @Inject(
        method = "render(Z)V",  // adjust method descriptor if needed
        at = @At("RETURN")
    )
    private void legacyperf$limitFps(boolean tick, CallbackInfo ci) {
        int limit = LegacyPerfConfig.fpsLimit;
        if (limit <= 0) return;

        long targetNs  = 1_000_000_000L / limit;
        long now       = System.nanoTime();
        long sinceLastNs = now - legacyperf$lastFrameNs;
        long sleepNs   = targetNs - sinceLastNs;

        if (sleepNs > 500_000L) { // only bother if >0.5 ms remains
            try {
                long ms   = sleepNs / 1_000_000L;
                int  nanos = (int)(sleepNs % 1_000_000L);
                Thread.sleep(ms, nanos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        legacyperf$lastFrameNs = System.nanoTime();
    }
}
