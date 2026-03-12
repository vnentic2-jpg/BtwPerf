package com.legacyperf.mixin;

import com.legacyperf.LegacyPerf;
import com.legacyperf.config.LegacyPerfConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Memory Optimizer Mixin
 * =======================
 * Target: net.minecraft.client.MinecraftClient
 * Method: tick() – the game-logic tick, called 20×/second.
 *
 * Periodically checks the JVM's free heap. If available memory falls below the
 * configured threshold, calls System.gc() to nudge a collection.
 * This does NOT guarantee GC runs immediately; it simply hints the JVM.
 *
 * The interval is configurable (default: every 100 ticks = 5 seconds) so that
 * gc hint overhead is negligible.
 *
 * BTWCE compatibility: Completely safe – tick() is not modified in any
 * disruptive way; we only read Runtime state and occasionally call gc().
 *
 * Mapping note:
 *   net.minecraft.client.MinecraftClient  (vanilla: net.minecraft.client.Minecraft)
 *   Method: tick()V
 */
@Mixin(targets = "net.minecraft.client.MinecraftClient")
public class MemoryOptimizerMixin {

    private int legacyperf$tickCounter = 0;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void legacyperf$memoryCheck(CallbackInfo ci) {
        if (!LegacyPerfConfig.memoryOptimizer) return;

        legacyperf$tickCounter++;
        if (legacyperf$tickCounter < LegacyPerfConfig.memoryCheckIntervalTicks) return;
        legacyperf$tickCounter = 0;

        Runtime rt         = Runtime.getRuntime();
        long freeMb        = (rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / (1024L * 1024L);
        long thresholdMb   = LegacyPerfConfig.memoryThresholdMb;

        if (freeMb < thresholdMb) {
            System.out.println("[LegacyPerf] Free heap " + freeMb + "MB < threshold " + thresholdMb + "MB - hinting GC");
            System.gc();
        }
    }
}
