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
            float spoilage = 1.0F - ratio;

            int baseColor;
            if (ratio >= 0.66F) {
                baseColor = 0x00a634; // Зеленый
            } else if (ratio >= 0.33F) {
                baseColor = 0xc99700; // Желтый
            } else {
                baseColor = 0xbf0000; // Красный
            }

            // Формируем ARGB цвет
            int finalColor = (0xD2 << 24) | baseColor;

            // Рисуем фон 16x16 позади предмета
            graphics.fill(x, y, x+16, (int) (y + (16 * spoilage)), 0xAA000000);
            graphics.fill(x, (int) (y + (16 * spoilage)), x + 16, y + 16, finalColor);
        }
    }
}