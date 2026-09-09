package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHooks.class)
public class ClientHooksMixin {
    @Inject(
            method = "shouldCauseReequipAnimation",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mikpik$preventReequipOnSpoilTime(
            ItemStack from, ItemStack to, int slot, CallbackInfoReturnable<Boolean> cir) {
        if (from.isEmpty() || to.isEmpty()) return;
        if (!ItemStack.isSameItem(from, to)) return;

        ItemStack fromCopy = from.copy();
        ItemStack toCopy = to.copy();

        fromCopy.remove(ModDataComponents.SPOIL_TIME_REMAINING.get());
        toCopy.remove(ModDataComponents.SPOIL_TIME_REMAINING.get());

        fromCopy.remove(ModDataComponents.SPOIL_LAST_REDUCTION.get());
        toCopy.remove(ModDataComponents.SPOIL_LAST_REDUCTION.get());

        fromCopy.remove(ModDataComponents.HEART_CHARGE.get());
        toCopy.remove(ModDataComponents.HEART_CHARGE.get());

        if (ItemStack.isSameItemSameComponents(fromCopy, toCopy)) {
            cir.setReturnValue(false);
        }
    }
}
