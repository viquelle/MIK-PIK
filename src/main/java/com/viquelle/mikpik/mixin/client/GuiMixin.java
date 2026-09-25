package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import com.viquelle.mikpik.item.FreshnessManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.util.SpoilBackground;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    private static final ResourceLocation PENALTY_FULL =
            ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_full.png");
    private static final ResourceLocation PENALTY_HALF_LEFT =
            ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_half_left.png");
    private static final ResourceLocation PENALTY_HALF_RIGHT =
            ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_half_right.png");

    private static final ResourceLocation HEART_CONTAINER =
            ResourceLocation.withDefaultNamespace("hud/heart/container");

    @Inject(method = "renderHealthLevel", at = @At("TAIL"))
    private void renderPenalty(GuiGraphics p_283143_, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;

        AttributeModifier modifier = attr.getModifier(HealthPenailtyUtil.PENALTY_ID);
        if (modifier == null) return;

        double penalty = -modifier.amount();
        if (penalty <= 0.001) return;

        renderPenaltyHearts(p_283143_, player, penalty);
    }

    private void renderPenaltyHearts(GuiGraphics graphics, Player player, double penalty) {
        double currentMax = player.getMaxHealth();
        double originalMax = currentMax / (1f - penalty);
        currentMax = Math.round(currentMax * 1000.0) / 1000.0;
        originalMax = Math.round(originalMax * 1000.0) / 1000.0;

        int x = graphics.guiWidth() / 2 - 91;
        int y = graphics.guiHeight() - 39;

        // Находим диапазон сердец, которые частично или полностью находятся в штрафе
        int startHeart = Mth.floor(currentMax / 2.0f);
        int endHeart = Mth.ceil(originalMax / 2.0f);
        //MikpikMod.LOGGER.info("{} {} {} {}", currentMax, originalMax, startHeart, endHeart);
        for (int heartIndex = endHeart - 1; heartIndex >= startHeart; heartIndex--) {
            int leftHalfIndex = heartIndex * 2;
            int rightHalfIndex = heartIndex * 2 + 1;

            int posX = x + (heartIndex % 10) * 8;
            int posY = y - (heartIndex / 10) * 10;

            if (rightHalfIndex > originalMax) {
                graphics.blitSprite(HEART_CONTAINER, posX, posY, 9, 9);
                graphics.blit(PENALTY_HALF_LEFT, posX, posY, 0, 0, 9, 9, 9, 9);
            } else {
                if (leftHalfIndex < currentMax) {
                    graphics.blit(PENALTY_HALF_RIGHT, posX, posY, 0, 0, 9, 9, 9, 9);
                } else {
                    graphics.blitSprite(HEART_CONTAINER, posX, posY, 9, 9);
                    graphics.blit(PENALTY_FULL, posX, posY, 0, 0, 9, 9, 9, 9);
                }
            }
        }
    }

    @Inject(
            method = {"renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity; Lnet/minecraft/world/item/ItemStack;III)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed, CallbackInfo CI) {
        SpoilBackground.drawSpoilageBackground(guiGraphics, stack, x, y);
    }
}