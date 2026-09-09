package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemSpoilageMixin {

    @Inject(method = "isBarVisible", at = @At("RETURN"), cancellable = true)
    public void isBarVisible(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.has(ModDataComponents.SPOIL_TIME.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBarWidth", at = @At("RETURN"), cancellable = true)
    public void getBarWidth(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.has(ModDataComponents.SPOIL_TIME.get())) {
            int spoilTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME.get(), 1);
            float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) spoilTime);
            float ratio = Math.max(0.0F, Math.min(1.0F, timeRemaining / spoilTime));
            cir.setReturnValue(Math.round(13.0F * ratio));
        }
    }

    @Inject(method = "getBarColor", at = @At("RETURN"), cancellable = true)
    public void getBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.has(ModDataComponents.SPOIL_TIME.get())) {
            int spoilTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME.get(), 1);
            float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) spoilTime);
            float ratio = Math.max(0.0F, Math.min(1.0F, timeRemaining / spoilTime));

            if (ratio >= 0.66F) {
                cir.setReturnValue(0x00FF00); // Зеленый
            } else if (ratio >= 0.33F) {
                cir.setReturnValue(0xFFFF00); // Желтый
            } else {
                cir.setReturnValue(0xFF0000); // Красный
            }
        }
    }
}