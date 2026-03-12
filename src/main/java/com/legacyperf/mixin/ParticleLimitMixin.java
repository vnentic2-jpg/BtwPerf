package com.legacyperf.mixin;

import com.legacyperf.config.LegacyPerfConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_503")
public class ParticleLimitMixin {

    private static int legacyperf$particleCount = 0;
    private static long legacyperf$lastReset = 0;

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true, require = 0)
    private void legacyperf$limitParticles(Object entity, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        if (now - legacyperf$lastReset > 1000) {
            legacyperf$particleCount = 0;
            legacyperf$lastReset = now;
        }
        int limit = LegacyPerfConfig.PARTICLE_LIMIT;
        if (limit > 0 && legacyperf$particleCount >= limit) {
            ci.cancel();
            return;
        }
        legacyperf$particleCount++;
    }
}
