package com.legacyperf.mixin;

import com.legacyperf.config.LegacyPerfConfig;
import com.legacyperf.util.PerfFrustum;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Particle Limiting + Culling Mixin
 * ===================================
 * Target: net.minecraft.client.particle.ParticleManager
 *   (vanilla name: EffectRenderer)
 *
 * Two optimisations:
 *  1. addParticle – reject new particles when the count is already at the cap.
 *  2. renderParticles – skip rendering individual particles outside the frustum.
 *
 * BTWCE compatibility: BTWCE registers custom particle types but they all go
 * through addParticle(), so the cap applies uniformly. No BTWCE particle class
 * is directly targeted; only the manager is touched.
 *
 * Mapping note: In Legacy Fabric 1.6.4 barn mappings the class is commonly
 *   net.minecraft.client.particle.ParticleManager
 * The particle list field is typically named `particles` (a List<List<Particle>>
 * split by render layer, 0..3). Adjust the @Shadow target if your build differs.
 */
@Mixin(targets = "net.minecraft.client.particle.ParticleManager")
public class ParticleLimitMixin {

    /**
     * The per-layer particle lists. In vanilla 1.6.4 there are 4 render layers.
     * Barn mapping: field name may be "particles" or "fxLayers" – check decompile.
     */
    @Shadow
    private List<Particle>[] particles;

    // ── 1. Cap particle count ─────────────────────────────────────────────────

    /**
     * Injected at HEAD of addParticle (addEffect in some mappings).
     * Counts all live particles; if the total exceeds the cap, cancels the add.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void legacyperf$limitParticles(Particle particle, CallbackInfo ci) {
        if (!LegacyPerfConfig.particleLimit) return;
        if (particles == null) return;

        int total = 0;
        for (List<Particle> layer : particles) {
            if (layer != null) total += layer.size();
        }
        if (total >= LegacyPerfConfig.maxParticles) {
            ci.cancel();
        }
    }

    // ── 2. Cull off-screen particles during render ────────────────────────────

    /**
     * Wraps the per-particle render call to skip particles outside the frustum.
     *
     * In 1.6.4 the particle render loop is inside renderParticles / render.
     * We inject into Particle#render (the instance method on each particle)
     * from within this manager; a simpler approach is to inject into the
     * manager's own render loop – see the AT below.
     *
     * Alternative: if the method signature differs, target
     *   "renderParticles(Lnet/minecraft/entity/Entity;F)V"
     * and walk the list manually.
     */
    @Inject(
        method = "renderParticles(Lnet/minecraft/entity/Entity;F)V",
        at = @At("HEAD")
    )
    private void legacyperf$preRenderParticles(Object viewer, float tickDelta, CallbackInfo ci) {
        // Nothing special needed at the start of the render method;
        // the actual per-particle frustum check is done in the Particle render
        // mixin (ParticleRenderMixin) to avoid iterating twice.
        // This hook is reserved for future pre-render work (e.g. sorting).
    }
}
