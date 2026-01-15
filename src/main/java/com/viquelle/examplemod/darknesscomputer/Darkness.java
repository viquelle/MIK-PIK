package com.viquelle.examplemod.darknesscomputer;

import com.viquelle.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class Darkness {
    static Logger log = LogManager.getLogger("Darkness");
    private static float[][] LUMINANCE = new float[16][16];
    public static boolean enabled = true;

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!Darkness.enabled) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        // Используем современную проверку NeoForge
        if (player.getEyeInFluidType() == (net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value())) {
            // Делаем воду темной (почти черной)
            ExampleMod.LOGGER.info("{} {} {}", event.getRed(), event.getGreen(), event.getBlue());
            event.setRed(0.01f);
            event.setGreen(0.01f);
            event.setBlue(0.02f);
        }
    }

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        if (!Darkness.enabled) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.getEyeInFluidType() == (net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value())) {
            // Ограничиваем дистанцию рендера, чтобы не было "рентгена"
            event.setNearPlaneDistance(2.0f);
            event.setFarPlaneDistance(10.0f); // Дальше 6 блоков - стена тьмы
            event.setCanceled(true); // Сообщаем движку, что мы перехватили рендер
        }
    }

    public static float skyFactor(Level world) {
        if (world == null) return -1;
        if (world.dimensionType().hasSkyLight()) {
            final float time = world.getTimeOfDay(0);
            if (time > 0.25f && time < 0.75f) {
                final float oldWeight = Math.max(0, (Math.abs(time - 0.5f) - 0.2f)) * 20;
                final float moon = world.getMoonBrightness();
                return Mth.lerp(oldWeight * oldWeight * oldWeight, moon * moon, 1f);
            } else {
                return 1;
            }
        }
        return 0;
    }

    public static void updateLuminance(Minecraft client, GameRenderer worldRenderer, float tickDelta, float prevFlicker) {
        final ClientLevel world = client.level;

        if (world != null) {
            if (client.player.hasEffect(MobEffects.NIGHT_VISION) || client.player.hasEffect(MobEffects.CONDUIT_POWER) || world.getSkyFlashTime() > 0) {
                enabled = false;
                return;
            } else {
                enabled = true;
            }

            float waterFactor = 1.0f;
            if (!client.player.getEyeInFluidType().isAir()) {

            }

            final float dimSkyFactor = Darkness.skyFactor(world);
            final float ambient = world.getSkyDarken(1.0f);
            final DimensionType dim = world.dimensionType();
            final boolean blockAmbient = true;

            for (int skyIndex = 0; skyIndex < 16; skyIndex++) {
                float skyFactor = 1f - skyIndex / 15f;
                skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
                skyFactor *= dimSkyFactor;

                float min = skyFactor * 0.05f;
                final float rawAmbient = ambient * skyFactor;
                final float minAmbient = rawAmbient * (1 - min) + min;

                final float skyBase = LightTexture.getBrightness(dim, skyIndex) * minAmbient;

                min = 0.35f * skyFactor;
                float skyRed = skyBase * (rawAmbient * (1 - min) + min);
                float skyGreen = skyBase * (rawAmbient * (1 - min) + min);
                float skyBlue = skyBase;

                if (worldRenderer.getDarkenWorldAmount(tickDelta) > 0.0f) {
                    final float skyDarkness = worldRenderer.getDarkenWorldAmount(tickDelta);
                    skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
                    skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
                    skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
                }

                for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
                    float blockFactor = 1f;

                    if (blockAmbient) {
                        blockFactor = 1f - blockIndex / 15f;
                        blockFactor = 1 - blockFactor * blockFactor * blockFactor * blockFactor;
                    }

                    final float blockBase = blockFactor * LightTexture.getBrightness(dim, blockIndex) * (prevFlicker * 0.1f + 1.5f);
                    min = 0.4f * blockFactor;
                    final float blockGreen = blockBase * ((blockBase * (1 - min) + min) * (1 - min) + min);
                    final float blockBlue = blockBase * blockBase * blockBase * ((1 - min) + min);

                    float red = skyRed + blockBase;
                    float green = skyGreen + blockGreen;
                    float blue = skyBlue + blockBlue;

                    final float f = Math.max(skyFactor, blockFactor);
                    min = 0.03f * f;
                    red = red * (0.99F - min) + min;
                    green = green * (0.99F - min) + min;
                    blue = blue * (0.99F - min) + min;

                    if (world.dimension() == Level.END) {
                        red = skyFactor * 0.22F + blockBase * 0.75F;
                        green = skyFactor * 0.28F + blockGreen * 0.75F;
                        blue = skyFactor * 0.25F + blockBlue * 0.75F;
                    }

                    red = Math.min(1.0F,red);
                    green = Math.min(1.0F,green);
                    blue = Math.min(1.0F,blue);

                    final float gamma = client.options.gamma().get().floatValue() * f;
                    float invRed = 1.0F - red;
                    float invGreen = 1.0F - green;
                    float invBlue = 1.0F - blue;
                    invRed = 1.0F - invRed * invRed * invRed * invRed;
                    invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
                    invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
                    red = red * (1.0F - gamma) + invRed * gamma;
                    green = green * (1.0F - gamma) + invGreen * gamma;
                    blue = blue * (1.0F - gamma) + invBlue * gamma;

                    min = 0.03f * f;
                    red = red * (0.99F - min) + min;
                    green = green * (0.99F - min) + min;
                    blue = blue * (0.99F - min) + min;

                    red = Math.max(Math.min(1.0F, red),0F);
                    blue = Math.max(Math.min(1.0F, blue),0F);
                    green = Math.max(Math.min(1.0F, green),0F);

                    LUMINANCE[blockIndex][skyIndex] = Darkness.luminance(red,green,blue);
                }
            }
        }
    }

    public static int darken(int color, int blockIndex, int skyIndex) {
        final float lTarget = LUMINANCE[blockIndex][skyIndex];
        final float r = (color & 0xFF) / 255f;
        final float g = ((color >> 8) & 0xFF) / 255f;
        final float b = ((color >> 16) & 0xFF) / 255f;
        final float l = luminance(r,g,b);
        final float f = l > 0 ? Math.min(1, lTarget/ l) : 0;

        return f == 1f ? color : 0xFF000000 | Math.round(f * r * 255) | Math.round(f * g * 255) << 8 | Math.round(f * b * 255) << 16;
    }

    public static float luminance(float r, float g, float b) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }


}
