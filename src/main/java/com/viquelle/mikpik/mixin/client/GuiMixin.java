package com.viquelle.mikpik.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import com.viquelle.mikpik.item.FreshnessManager;
import com.viquelle.mikpik.registry.ModAttachments;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.util.SpoilBackground;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.http.util.Args;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique
    private static final ResourceLocation PENALTY_FULL = ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_full.png");
    @Unique
    private static final ResourceLocation PENALTY_HALF_LEFT = ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_half_left.png");
    @Unique
    private static final ResourceLocation PENALTY_HALF_RIGHT = ResourceLocation.fromNamespaceAndPath("mikpik", "textures/gui/heart_penalty_half_right.png");

    @WrapOperation(
            method = "renderHealthLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"
            )
    )
    private double mikpik$modifyMaxHealth(
            Player player,
            Holder<Attribute> attribute,
            Operation<Double> original
    ) {
        double maxHealth = original.call(player, attribute);
        double penalty = player.getData(ModAttachments.PENALTY.get());
        maxHealth = maxHealth / (1 - penalty);

        return maxHealth;
    }

    @WrapOperation(
            method = "renderHearts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V"
            )
    )
    private void mikpik$renderHeart(
            Gui gui,
            GuiGraphics guiGraphics,
            Gui.HeartType heartType,
            int heartX,
            int heartY,
            boolean hardcore,
            boolean halfHeart,
            boolean blinking,
            Operation<Void> original,
            @Local(index = 2) Player player,
            @Local(index = 7) float maxHealth,
            @Local(index = 17) int l
    ) {
        original.call(
                gui,
                guiGraphics,
                heartType,
                heartX,
                heartY,
                hardcore,
                halfHeart,
                blinking
        );

        if (heartType != Gui.HeartType.CONTAINER) return;

        double actualMaxHealth = player.getMaxHealth();
        double penaltyHealth = maxHealth - actualMaxHealth;

        if (penaltyHealth <= 0.0) return;

        int penaltyHalfHearts = (int) Math.ceil(penaltyHealth);
        int firstPenaltyHeart = (int) Math.floor(actualMaxHealth / 2.0);
        int penaltyOffset = l - firstPenaltyHeart;
        if (penaltyOffset < 0) return;

        boolean actualMaxHasHalf = ((int) Math.ceil(actualMaxHealth)) % 2 == 1;
        int consumedHalfHearts;
        if (actualMaxHasHalf) {
            if (penaltyOffset == 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 1);

                guiGraphics.blit(
                        PENALTY_HALF_RIGHT,
                        heartX,
                        heartY,
                        0,
                        0,
                        9,
                        9,
                        9,
                        9
                );
                guiGraphics.pose().popPose();
                return;
            }

            consumedHalfHearts = 1 + (penaltyOffset - 1) * 2;
        } else {
            consumedHalfHearts = penaltyOffset * 2;
        }

        int remainingHalfHearts = penaltyHalfHearts - consumedHalfHearts;
        if (remainingHalfHearts >= 2) {
            guiGraphics.blit(
                    PENALTY_FULL,
                    heartX,
                    heartY,
                    0,
                    0,
                    9,
                    9,
                    9,
                    9
            );
        } else if (remainingHalfHearts == 1) {
            guiGraphics.blit(
                    PENALTY_HALF_LEFT,
                    heartX,
                    heartY,
                    0,
                    0,
                    9,
                    9,
                    9,
                    9
            );
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