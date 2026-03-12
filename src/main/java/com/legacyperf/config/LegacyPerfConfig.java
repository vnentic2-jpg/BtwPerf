package com.legacyperf.config;

import com.legacyperf.LegacyPerf;

import java.io.*;
import java.util.Properties;

/**
 * Simple properties-based config stored at config/legacyperf.properties.
 * All options can be toggled without restarting the game via /legacyperf reload
 * (if the command module is installed), or by editing the file and restarting.
 */
public class LegacyPerfConfig {

    private static final File CONFIG_FILE = new File("config/legacyperf.properties");

    // ── Entity culling ─────────────────────────────────────────────────────────
    /** Skip rendering entities that are outside the view frustum. */
    public static boolean entityCulling = true;

    /**
     * Extra padding (in blocks) added to entity bounding boxes before
     * the frustum test. Increase if entities flicker at screen edges.
     */
    public static double entityCullingPadding = 1.5;

    // ── Particle limiting ──────────────────────────────────────────────────────
    /** Cap the number of simultaneously active particles. */
    public static boolean particleLimit = true;

    /** Maximum number of particles allowed at once. Vanilla default ≈ 4000. */
    public static int maxParticles = 500;

    /**
     * Also cull particles that are behind the camera or beyond render distance.
     * Disable if some BTWCE particles appear to vanish unexpectedly.
     */
    public static boolean particleCulling = true;

    // ── FPS limiter ────────────────────────────────────────────────────────────
    /**
     * Cap the frame rate to this value (frames per second).
     * Set to 0 to disable the limiter (uncapped).
     * Useful for reducing heat / power usage on fast machines.
     */
    public static int fpsLimit = 0;

    // ── Chunk culling ──────────────────────────────────────────────────────────
    /**
     * Skip rebuild-scheduling for chunk sections whose AABB is entirely
     * outside the view frustum. Safe with BTWCE; complements its own culling.
     */
    public static boolean chunkCulling = true;

    // ── Memory optimizer ───────────────────────────────────────────────────────
    /**
     * Hint the JVM to run GC when free heap drops below memoryThresholdMb.
     * Helps avoid sudden lag spikes on memory-constrained setups.
     */
    public static boolean memoryOptimizer = true;

    /**
     * Free heap threshold (MiB) that triggers a GC hint.
     * Lowering this reduces how often GC is nudged.
     */
    public static int memoryThresholdMb = 64;

    // ── Internal ───────────────────────────────────────────────────────────────
    /** Interval (ticks) between memory checks. 20 ticks = 1 second. */
    public static int memoryCheckIntervalTicks = 100;

    // ──────────────────────────────────────────────────────────────────────────

    public static void load() {
        Properties props = new Properties();

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                props.load(reader);
            } catch (IOException e) {
                System.err.println("[LegacyPerf] Failed to read config, using defaults: " + e.getMessage());            }
        }

        entityCulling           = bool(props, "entityCulling",            entityCulling);
        entityCullingPadding    = dbl (props, "entityCullingPadding",     entityCullingPadding);
        particleLimit           = bool(props, "particleLimit",            particleLimit);
        maxParticles            = intv(props, "maxParticles",             maxParticles);
        particleCulling         = bool(props, "particleCulling",          particleCulling);
        fpsLimit                = intv(props, "fpsLimit",                 fpsLimit);
        chunkCulling            = bool(props, "chunkCulling",             chunkCulling);
        memoryOptimizer         = bool(props, "memoryOptimizer",          memoryOptimizer);
        memoryThresholdMb       = intv(props, "memoryThresholdMb",        memoryThresholdMb);
        memoryCheckIntervalTicks= intv(props, "memoryCheckIntervalTicks", memoryCheckIntervalTicks);

        save(); // write defaults for any missing keys
    }

    public static void save() {
        CONFIG_FILE.getParentFile().mkdirs();
        Properties props = new Properties();

        props.setProperty("entityCulling",            String.valueOf(entityCulling));
        props.setProperty("entityCullingPadding",     String.valueOf(entityCullingPadding));
        props.setProperty("particleLimit",            String.valueOf(particleLimit));
        props.setProperty("maxParticles",             String.valueOf(maxParticles));
        props.setProperty("particleCulling",          String.valueOf(particleCulling));
        props.setProperty("fpsLimit",                 String.valueOf(fpsLimit));
        props.setProperty("chunkCulling",             String.valueOf(chunkCulling));
        props.setProperty("memoryOptimizer",          String.valueOf(memoryOptimizer));
        props.setProperty("memoryThresholdMb",        String.valueOf(memoryThresholdMb));
        props.setProperty("memoryCheckIntervalTicks", String.valueOf(memoryCheckIntervalTicks));

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer,
                "LegacyPerf configuration\n" +
                "# All boolean options accept: true / false\n" +
                "# Reload by restarting, or use /legacyperf reload if the command addon is present");
        } catch (IOException e) {
            System.err.println("[LegacyPerf] Failed to save config: " + e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static boolean bool(Properties p, String key, boolean def) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(def)));
    }

    private static int intv(Properties p, String key, int def) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }

    private static double dbl(Properties p, String key, double def) {
        try { return Double.parseDouble(p.getProperty(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
}
