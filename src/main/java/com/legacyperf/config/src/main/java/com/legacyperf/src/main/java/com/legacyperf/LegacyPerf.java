package com.legacyperf;

import com.legacyperf.config.LegacyPerfConfig;
import net.fabricmc.api.ClientModInitializer;

public class LegacyPerf implements ClientModInitializer {

    public static final String MOD_ID = "legacyperf";

    @Override
    public void onInitializeClient() {
        System.out.println("[LegacyPerf] Initializing...");
        LegacyPerfConfig.load();
        System.out.println("[LegacyPerf] Config loaded. Entity culling: "
            + LegacyPerfConfig.entityCulling
            + ", Particle limit: " + LegacyPerfConfig.maxParticles
            + ", FPS limit: " + (LegacyPerfConfig.fpsLimit > 0 ? LegacyPerfConfig.fpsLimit : "off"));
    }
}
