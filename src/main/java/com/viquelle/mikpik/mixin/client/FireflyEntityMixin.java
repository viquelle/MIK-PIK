
package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.entity.firefly.FireflyEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireflyEntity.class)
public abstract class FireflyEntityMixin {

    @Inject(
            method = "isPickable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mikpik$pickableWithBottle(
            CallbackInfoReturnable<Boolean> cir
    ) {
        FireflyEntity firefly = (FireflyEntity) (Object) this;

        if (firefly.isDormant()) {
            cir.setReturnValue(false);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            cir.setReturnValue(false);
            return;
        }

        boolean hasBottle =
                minecraft.player.getItemInHand(InteractionHand.MAIN_HAND)
                        .is(Items.GLASS_BOTTLE)
                        || minecraft.player.getItemInHand(InteractionHand.OFF_HAND)
                        .is(Items.GLASS_BOTTLE);

        cir.setReturnValue(hasBottle);
    }
}
