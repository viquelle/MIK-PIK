package com.viquelle.examplemod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.client.sanity.SanityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class HudRenderer {
    private static final ResourceLocation SANITY_TEXTURE = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID,"textures/gui/sprite-0003-sheet.png");
    private static final ResourceLocation SANITY_CONTRAST_TEXTURE = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/gui/sprite-0003-contrast.png");
    private static final int ICON_SIZE = 64;

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        GuiGraphics gui = event.getGuiGraphics();

        float sanity = SanityData.getSanity();

        int spriteIndex = 0;

        if (sanity >= 95) spriteIndex = 0;
        else if (sanity >= 75) spriteIndex = 1;
        else if (sanity >= 50) spriteIndex = 2;
        else if (sanity >= 25) spriteIndex = 3;
        else if (sanity >= 5) spriteIndex = 4;
        else spriteIndex = 5;

        int u = spriteIndex * ICON_SIZE;
        int v = 0;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = screenWidth - ICON_SIZE - 8; // Отступ 8пх справа
        int y = 8; // Отступ 8пх свверху

        // LogUtils.getLogger().info("sanity: {} x: {} y: {} u: {} v: {}", sanity, x,y,u,v);

        if (!mc.options.hideGui) {
            RenderSystem.enableBlend();

            gui.blit(
                    SANITY_CONTRAST_TEXTURE,
                    x,y,
                    u,v,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE * 6, ICON_SIZE
            );

            gui.blit(
                    SANITY_TEXTURE,
                    x, y,
                    u, v,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE * 6, ICON_SIZE
            );

            RenderSystem.disableBlend();
        }
    }
}
