package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.FreshnessManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(GuiGraphics.class)
public class GuiGraphicsSpoilageMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("HEAD")
    )
    private void renderItem(
            @Nullable LivingEntity entity,
            @Nullable Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            int guiOffset,
            CallbackInfo ci
    ) {
        float z = ((GuiGraphics)(Object)this).pose().last().pose().m32();
        if (z >= 230f && z <= 235f) return; // Z-index курсора, хардкод
        drawSpoilageBackground((GuiGraphics) (Object) this, stack, x, y);
    }

    private void drawSpoilageBackground(GuiGraphics graphics, ItemStack stack, int x, int y) {
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