package com.fpsboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * FPS Boost - adaptive client-side performance tuner for Fabric.
 *
 * How it works:
 * - Measures real frame times each render/tick.
 * - If FPS is under target, it backs off render distance, particles,
 *   biome blend radius and graphics quality (in that order, cheapest-impact first).
 * - If FPS is comfortably above target, it slowly gives quality back.
 *
 * This does NOT fake a number, it genuinely reduces GPU/CPU work.
 * On weak/mobile hardware, don't expect a hard 200 fps guarantee -
 * the ceiling is still your device. This gets you as close as your
 * hardware allows without you tuning settings by hand every session.
 */
public class FpsBoostClient implements ClientModInitializer {

    public static FpsBoostConfig CONFIG;

    private static int frameCounter = 0;
    private static long lastFpsSampleTime = 0L;
    private static int currentFps = 0;

    private static long lastAdjustTime = 0L;
    private static final long ADJUST_INTERVAL_MS = 2500; // don't thrash settings every tick

    private static net.minecraft.client.option.KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        CONFIG = FpsBoostConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new net.minecraft.client.option.KeyBinding(
                "key.fpsboost.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.fpsboost"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        // Toggle handling
        while (toggleKey.wasPressed()) {
            CONFIG.enabled = !CONFIG.enabled;
            CONFIG.save();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("[FPS Boost] " + (CONFIG.enabled ? "Enabled" : "Disabled")),
                        true
                );
            }
        }

        if (!CONFIG.enabled || !CONFIG.adaptiveMode) return;

        // Sample fps roughly once a second by counting ticks against wall clock frames.
        frameCounter++;
        long now = System.currentTimeMillis();
        if (now - lastFpsSampleTime >= 1000) {
            currentFps = client.getCurrentFps();
            lastFpsSampleTime = now;
            frameCounter = 0;

            if (now - lastAdjustTime >= ADJUST_INTERVAL_MS) {
                adjust(client);
                lastAdjustTime = now;
            }
        }
    }

    private void adjust(MinecraftClient client) {
        var options = client.options;
        int target = CONFIG.targetFps;

        boolean belowTarget = currentFps < target;
        boolean wellAboveTarget = currentFps > target + 40;

        if (belowTarget) {
            // Step 1: render distance down
            int rd = options.getViewDistance().getValue();
            if (rd > CONFIG.minRenderDistance) {
                options.getViewDistance().setValue(Math.max(CONFIG.minRenderDistance, rd - 1));
            } else if (CONFIG.reduceParticles && options.getParticles().getValue() != ParticlesMode.MINIMAL) {
                options.getParticles().setValue(ParticlesMode.MINIMAL);
            } else if (CONFIG.capBiomeBlend && options.getBiomeBlendRadius().getValue() > 0) {
                options.getBiomeBlendRadius().setValue(0);
            } else if (CONFIG.disableFancyGraphics && options.getGraphicsMode().getValue() != GraphicsMode.FAST) {
                options.getGraphicsMode().setValue(GraphicsMode.FAST);
            }
        } else if (wellAboveTarget) {
            // Give quality back gradually, render distance first (most visible)
            int rd = options.getViewDistance().getValue();
            if (rd < CONFIG.maxRenderDistance) {
                options.getViewDistance().setValue(rd + 1);
            }
        }
    }
}
package
