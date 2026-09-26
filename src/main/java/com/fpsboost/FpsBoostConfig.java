package com.fpsboost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed config. No external config-library dependency needed.
 */
public class FpsBoostConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fpsboost.json");

    public boolean enabled = true;
    public boolean adaptiveMode = true; // auto-tune settings to hit targetFps
    public int targetFps = 200;
    public int minRenderDistance = 4;
    public int maxRenderDistance = 12;
    public boolean disableFancyGraphics = true;
    public boolean reduceParticles = true;
    public boolean capBiomeBlend = true;

    public static FpsBoostConfig load() {
        try {
            if (Files.exists(PATH)) {
                String json = Files.readString(PATH);
                FpsBoostConfig cfg = GSON.fromJson(json, FpsBoostConfig.class);
                if (cfg != null) return cfg;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FpsBoostConfig fresh = new FpsBoostConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
