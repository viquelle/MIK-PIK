package com.viquelle.examplemod.darknesscomputer;

import com.viquelle.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Darkness {
    static Logger log = LogManager.getLogger("Darkness");
    private static float[][] LUMINANCE = new float[16][16];
    public static boolean enabled = true;

    public static float skyFactor(Level world) {
        if (world == null) return -1;
        if (world.dimensionType().hasSkyLight()) {
            final float time = world.getTimeOfDay(0);
            if (time > 0.25f && time < 0.75f) { log.info("skebob"); }
            log.info("TIME {}",time);
            return 0;
        }
        return 0;
    }

    public static void updateLuminance(Minecraft client) {
        final ClientLevel world = client.level;

        if (world != null) {
            boolean a1 = client.player.hasEffect(MobEffects.NIGHT_VISION);
            boolean a2 = client.player.hasEffect(MobEffects.CONDUIT_POWER);
            float a3 = client.player.getWaterVision();
            var a4 = world.getSkyFlashTime();
            var a5 = world.getMoonBrightness();
            var a6 = world.getSkyDarken();
            var a7 = world.isNight();
            log.info("{} {} {} {} {} {} {}",a1,a2,a3,a4,a5,a6,a7);
        }
    }

    public static int darken(int color, int blockIndex, int skyIndex) {
        final float lTarget = LUMINANCE[blockIndex][skyIndex];
        final float r = (color & 0xFF) / 255f;
        final float g = ((color >> 8) & 0xFF) / 255f;
        final float b = ((color >> 16) & 0xFF) / 255f;
        final float l = luminance(r,g,b);
        final float f = l > 0 ? Math.min(1, lTarget/)
    }

    public static float luminance(float r, float g, float b) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }
}
