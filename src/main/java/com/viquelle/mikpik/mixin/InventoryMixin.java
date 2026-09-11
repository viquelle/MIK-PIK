package com.viquelle.mikpik.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.FreshnessManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "hasRemainingSpaceForItem", at = @At("HEAD"), cancellable = true)
    private void onHasRemainingSpaceForItem(ItemStack destination, ItemStack origin, CallbackInfoReturnable<Boolean> cir) {
        if (!ItemStack.isSameItem(origin, destination)) return;
        if (destination.isEmpty() || !destination.isStackable() || destination.getCount() == destination.getMaxStackSize()) return;

        int destTime = FreshnessManager.shouldSpoiling(destination);
        if (destTime <= 0) return;

        int origTime = FreshnessManager.shouldSpoiling(origin);
        if (origTime <= 0) return;

        float destRemain = destination.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)destTime) / destTime;
        float origRemain = origin.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)origTime) / origTime;

        if (Math.abs(origRemain - destRemain) < 0.1) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "addResource(ILnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V")
    )
    private void onAddResourceBeforeGrow(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Inventory inv = (Inventory)(Object)this;
        ItemStack itemstack = inv.getItem(slot);

        int spoilTime1 = FreshnessManager.shouldSpoiling(itemstack); // Если у таргетного нету таких данных, то нам нечего тут делать
        if (spoilTime1 <= 0) return;
        int spoilTime2 = FreshnessManager.shouldSpoiling(stack); // Если у таргетного есть, а у даваемого нет - сделаем
        itemstack.set(ModDataComponents.SPOIL_TIME, spoilTime1);
        stack.set(ModDataComponents.SPOIL_TIME, spoilTime2);

        int k = Math.min(inv.getMaxStackSize() - itemstack.getCount(), stack.getCount());
        float totalDestTime = itemstack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)spoilTime1) * itemstack.getCount();
        float totalSrcTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)spoilTime2) * k;
        float avg = (totalDestTime + totalSrcTime) / (itemstack.getCount() + k);

        itemstack.set(ModDataComponents.SPOIL_TIME_REMAINING, avg);
        MikpikMod.LOGGER.info("[SPOIL] Новое среднее время порчи: {}", avg);
    }
}

