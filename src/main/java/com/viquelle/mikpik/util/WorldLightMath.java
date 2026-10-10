
package com.viquelle.mikpik.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Shared lighting calculations.
 *
 * Safe to use from both logical client and logical server.
 * This class must not depend on Minecraft client classes.
 */
public final class WorldLightMath {
    public static float skyFactor(Level world, float partialTick) {
        if (world == null || !world.dimensionType().hasSkyLight()) {
            return 0.0f;
        }

        float time = world.getTimeOfDay(partialTick);
        float moon = world.getMoonBrightness();
        moon *= moon;

        final float day = 1.0f;
        final float transition = 0.05f;

        if (time >= 0.25f - transition && time < 0.25f) {
            float t = (time - (0.25f - transition)) / transition;
            return Mth.lerp(t, day, moon);
        } else if (time >= 0.25f && time <= 0.75f - transition) {
            return moon;
        } else if (time > 0.75f - transition && time <= 0.75f) {
            float t = (time - (0.75f - transition)) / transition;
            return Mth.lerp(t, moon, day);
        }

        return day;
    }

    public static float posSkyLight(Vec3 pos, Level world, float partialTick) {
        if (world == null || pos == null) return 0.0f;
        float rawSkyLight = world.getBrightness(LightLayer.SKY, BlockPos.containing(pos));
        return rawSkyLight * skyFactor(world, partialTick);
    }

    public static int calculateLightByHeight(BlockState state, int y) {
        double multiplier = Math.clamp(y / 64.0, 0.0, 1.0);

        return (int) Math.clamp(state.getLightEmission() * multiplier, 0, 15);
    }

    public static int calculateLightByHeight(BlockState state, int y, double minCoef) {
        double multiplier = Math.clamp(y / 64.0, minCoef, 1.0);
        return (int) Math.clamp(state.getLightEmission() * multiplier, 0, 15);
    }

    public static int calculateLightByHeight(BlockState state, int y, int minLight) {
        double multiplier = Math.clamp(y / 64.0, 0.0, 1.0);

        return (int) Math.clamp(state.getLightEmission() * multiplier, minLight, 15);
    }
}
