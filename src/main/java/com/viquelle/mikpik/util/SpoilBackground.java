package com.viquelle.mikpik.util;

import com.viquelle.mikpik.item.FreshnessManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class SpoilBackground {
    public static void drawSpoilageBackground(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) return;

        int maxSpoilTime = FreshnessManager.shouldSpoiling(stack);
        if (maxSpoilTime > 0) {
            float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) maxSpoilTime);
            float ratio = Math.clamp(timeRemaining / maxSpoilTime, 0.0F, 1.0F);

            int r, g, b;

            if (ratio >= 0.5F) {
                float t = (1.0F - ratio) / 0.5F;
                r = (int) (0 + (201 - 0) * t);
                g = (int) (166 + (151 - 166) * t);
                b = (int) (52 + (0 - 52) * t);
            } else {
                float t = (0.5F - ratio) / 0.5F;
                r = (int) (201 + (191 - 201) * t);
                g = (int) (151 + (0 - 151) * t);
                b = (int) (0 + (0 - 0) * t);
            }
            float alphaProgress = (1.0F - ratio) / 0.34F;
            alphaProgress = Math.clamp(alphaProgress, 0.0F, 1.0F);
            int alpha = (int) (alphaProgress * 0xAA);

            int finalColor = (alpha << 24) | (r << 16) | (g << 8) | b;


            // Рисуем фон 16x16 позади предмета
            graphics.fill(x, y, x+16, (int) (y + (16 * (1f - ratio))), 0xAA000000);
            graphics.fill(x, (int) (y + (16 * (1f - ratio))), x + 16, y + 16, finalColor);
        }
    }
}
