package com.legacyperf;

import com.legacyperf.config.LegacyPerfConfig;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LegacyPerf - Performance mod for Legacy Fabric 1.6.4
 * Compatible with BTWCE (Better Than Wolves Community Edition) and its addons.
 *
 * Features:
 *  - Entity frustum culling      (skip rendering off-screen entities)
 *  - Particle count limiting     (cap max simultaneous particles)
 *  - FPS limiter                 (reduce idle CPU/GPU burn)
 *  - Chunk distance culling      (skip obviously-hidden chunks)
 *  - Periodic memory optimizer   (hint GC when memory is low)
 */
public class LegacyPerf implements ClientModInitializer {

    public static final String MOD_ID = "legacyperf";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[LegacyPerf] Initializing performance optimizations...");
        LegacyPerfConfig.load();
        LOGGER.info("[LegacyPerf] Config loaded. Entity culling: {}, Particle limit: {}, FPS limit: {}",
                LegacyPerfConfig.entityCulling,
                LegacyPerfConfig.particleLimit,
                LegacyPerfConfig.fpsLimit > 0 ? LegacyPerfConfig.fpsLimit : "off");
    }
}
