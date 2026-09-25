package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.util.SpoilBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin({AbstractContainerScreen.class})
public class AbstractContainerScreenMixin {
    @Inject(
            method = "renderSlotContents(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;isFake()Z", shift = At.Shift.BEFORE)
    )
    private static void beforeIfCheck(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot, @Nullable String countString, CallbackInfo ci) {
        SpoilBackground.drawSpoilageBackground(guiGraphics, itemstack, slot.x, slot.y);
    }
}