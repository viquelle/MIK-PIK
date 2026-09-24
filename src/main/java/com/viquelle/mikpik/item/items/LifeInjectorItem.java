package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LifeInjectorItem extends Item {
    public static final ResourceLocation ANIM_LAYER_ID = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "injector_animation_layer");
    public static final ResourceLocation ANIM_ID = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "injector_using_anim");
    public static final ResourceLocation NONE_ANIM_ID = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "none");

    public LifeInjectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);

        if (level.isClientSide && player instanceof AbstractClientPlayer acp) {
            playAnimation(acp);
        }

        if (!level.isClientSide) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    SoundSource.PLAYERS,
                    0.35F,
                    0.75F
            );
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 50;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        stack.shrink(1);
        if (level.isClientSide && entity instanceof AbstractClientPlayer acp) {
            MikpikMod.LOGGER.info("finished using item");
            stopAnimation(acp);
        }

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            HealthPenailtyUtil.reducePenalty(player, 0.2);
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.BOTTLE_EMPTY,
                    SoundSource.PLAYERS,
                    0.3F,
                    0.75F
            );
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide && entity instanceof AbstractClientPlayer acp) {
            stopAnimation(acp);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (player.isUsingItem() && player.getUseItem() == stack) {
            int remaining = player.getUseItemRemainingTicks();
            if (remaining == 20) {
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ARMOR_EQUIP_LEATHER.value(),
                        SoundSource.PLAYERS,
                        0.3F,
                        0.8F
                );
            }
        }
    }

    public static void playAnimation(AbstractClientPlayer player) {
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, ANIM_LAYER_ID);
        if (controller != null) {
            if (controller.getCurrentAnimation() == null || !controller.getCurrentAnimation().animation().equals(PlayerAnimResources.getAnimation(ANIM_ID))) {
                controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
                controller.setFirstPersonConfiguration(
                        new FirstPersonConfiguration()
                                .setShowRightArm(true)
                                .setShowRightItem(true)
                );
                controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(4, EasingType.EASE_OUT_CIRC), ANIM_ID);
            }
        }
    }

    public static void stopAnimation(AbstractClientPlayer player) {
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, ANIM_LAYER_ID);
        if (controller != null && controller.getCurrentAnimation() != null) {
            if (controller.getCurrentAnimation().animation().equals(PlayerAnimResources.getAnimation(ANIM_ID))) {
                controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(1, EasingType.EASE_OUT_CIRC), NONE_ANIM_ID);
            }
        }
    }
}