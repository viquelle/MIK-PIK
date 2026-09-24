package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public class MultiPlayerGameMode {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ItemStack destroyingItem;

    @Inject(
            method = "startDestroyBlock",
            at = @At("HEAD"),
            cancellable = true)
    private void preventBreaking(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        assert minecraft.player != null;
        //MikpikMod.LOGGER.info("breaking {} {}", minecraft.player, GhostManager.isGhost(minecraft.player));
        if (GhostManager.isGhost(minecraft.player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "continueDestroyBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixContinue(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        assert minecraft.player != null;
        //MikpikMod.LOGGER.info("Continue {} {}", minecraft.player, GhostManager.isGhost(minecraft.player));
        if (GhostManager.isGhost(minecraft.player)) {
            cir.setReturnValue(false);
        }
        if (destroyingItem.isEmpty()) return;

        ItemStack current = minecraft.player.getMainHandItem();
        if (ItemStack.isSameItem(destroyingItem, current)) {
            if (current.has(ModDataComponents.SPOIL_TIME_REMAINING.get())) {
                this.destroyingItem.set(ModDataComponents.SPOIL_TIME_REMAINING.get(), current.get(ModDataComponents.SPOIL_TIME_REMAINING.get()));
            }
            if (current.has(ModDataComponents.SPOIL_LAST_REDUCTION.get())) {
                this.destroyingItem.set(ModDataComponents.SPOIL_LAST_REDUCTION.get(), current.get(ModDataComponents.SPOIL_LAST_REDUCTION.get()));
            }
        }
    }
}
