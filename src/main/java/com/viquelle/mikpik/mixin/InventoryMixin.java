package com.viquelle.mikpik.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.viquelle.mikpik.MikpikMod;
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
        if (!origin.has(ModDataComponents.SPOIL_TIME_REMAINING) || !destination.has(ModDataComponents.SPOIL_TIME_REMAINING) || !ItemStack.isSameItem(origin, destination)) return;
        if (destination.isEmpty() || !destination.isStackable() || destination.getCount() == destination.getMaxStackSize()) {
            return;
        }

        cir.setReturnValue(true);

        if (destination.getItem() == origin.getItem() &&
                destination.has(ModDataComponents.SPOIL_TIME_REMAINING) &&
                origin.has(ModDataComponents.SPOIL_TIME_REMAINING)) {

            float timeDest = destination.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, 0f);
            float timeOrigin = origin.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, 0f);

            if (timeDest > 0 && timeOrigin > 0) {
                float maxTime = Math.max(timeDest, timeOrigin);
                float diff = Math.abs(timeDest - timeOrigin);

                if (diff * 100 < maxTime * 5) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(
            method = "addResource(ILnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V")
    )
    private void onAddResourceBeforeGrow(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Inventory inv = (Inventory)(Object)this;
        ItemStack itemstack = inv.getItem(slot);
        if (itemstack.isEmpty()) return;

        int k = Math.min(inv.getMaxStackSize() - itemstack.getCount(), stack.getCount());
        if (itemstack.has(ModDataComponents.SPOIL_TIME_REMAINING) && stack.has(ModDataComponents.SPOIL_TIME_REMAINING)) {
            float totalDestTime = itemstack.get(ModDataComponents.SPOIL_TIME_REMAINING) * itemstack.getCount();
            float totalSrcTime = stack.get(ModDataComponents.SPOIL_TIME_REMAINING) * k;
            float avg = (totalDestTime + totalSrcTime) / (itemstack.getCount() + k);

            itemstack.set(ModDataComponents.SPOIL_TIME_REMAINING, avg);
            MikpikMod.LOGGER.info("[SPOIL] Новое среднее время порчи: {}", avg);
        }
    }
}

