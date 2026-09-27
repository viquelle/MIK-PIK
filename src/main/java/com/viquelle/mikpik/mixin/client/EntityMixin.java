package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.registry.ModItems;
import com.viquelle.mikpik.item.items.HeartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(
            method = "isCurrentlyGlowing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bob(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity)(Object)this;
        if (!(entity instanceof ItemEntity item)) return;

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;
        if (!GhostManager.isGhost(player)) return;
        if (!HeartItem.isCharged(item.getItem())) return;
        if (item.getItem().is(ModItems.HEART.get())) {
            cir.setReturnValue(true);
        }
    }


    @Inject(
            method = "getTeamColor",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostHeartColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity)(Object)this;

        if (!(entity instanceof ItemEntity item)) return;

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;
        if (!GhostManager.isGhost(player)) return;
        if (!HeartItem.isCharged(item.getItem())) return;
        if (item.getItem().is(ModItems.HEART.get())) {
            cir.setReturnValue(0xFF0000);
        }
    }

}
