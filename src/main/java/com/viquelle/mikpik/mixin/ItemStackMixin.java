package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true)
    private static void mikpik$allowSpoilStacking(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (1 == 1) return;

        if (stack1.isEmpty() || stack2.isEmpty()) return;
        if (!stack1.is(stack2.getItem())) return;

        boolean hasTime1 = stack1.has(ModDataComponents.SPOIL_TIME_REMAINING.get());
        boolean hasTime2 = stack2.has(ModDataComponents.SPOIL_TIME_REMAINING.get());

        if (hasTime1 && hasTime2) {
            ItemStack copy1 = stack1.copy();
            ItemStack copy2 = stack2.copy();

            copy1.remove(ModDataComponents.SPOIL_TIME_REMAINING.get());
            copy1.remove(ModDataComponents.SPOIL_LAST_REDUCTION.get());
            copy2.remove(ModDataComponents.SPOIL_TIME_REMAINING.get());
            copy2.remove(ModDataComponents.SPOIL_LAST_REDUCTION.get());

            if (ItemStack.isSameItemSameComponents(copy1, copy2)) {
                float time1 = stack1.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), 0f);
                float time2 = stack2.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), 0f);

                float diff = Math.abs(time1 - time2);
                float maxTime = Math.max(time1, time2);

                // Допуск 1-2.5%
                if (maxTime == 0 || (diff / maxTime) <= 0.025f) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}