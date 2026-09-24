package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class HeartItem extends Item {
    private static final Integer TARGET_CHARGE = 20; // equal HP
    public HeartItem(Properties properties) {
        super(properties);
    }

    public static float getCharge(ItemStack stack) {
        return stack.getOrDefault(
                ModDataComponents.HEART_CHARGE.get(),
                0f
        );
    }

    public static void setCharge(ItemStack stack, float charge) {
        stack.set(
                ModDataComponents.HEART_CHARGE.get(),
                charge
        );
    }

    public static void addCharge(ItemStack stack, float amount) {
        setCharge(stack, getCharge(stack) + amount);
    }

    public static boolean isCharged(ItemStack stack) {
        return getCharge(stack) >= TARGET_CHARGE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !isCharged(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * Math.clamp(getCharge(stack)/TARGET_CHARGE,0.0f,1.0f));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isCharged(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.getHealth() > 1) {
            player.startUsingItem(hand);
            return InteractionResultHolder.pass(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private static boolean consumeHealth(Player player, ItemStack stack) {
        float health = player.getHealth();
        if (health <= 1) return false;
        float itemNeed = TARGET_CHARGE - getCharge(stack);
        float canConsume = (float) Math.min(
                Math.clamp(health - 1, 0, 5.0),
                itemNeed
        );
        player.setHealth(health - canConsume);
        addCharge(stack, canConsume);
        float pitch = 1.2F - (itemNeed / TARGET_CHARGE) * 0.4F;
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SKELETON_AMBIENT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1F,
                pitch
        );

        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingTicks) {
        if (level.isClientSide) return;
        if (!(living instanceof Player player)) return;
        if (isCharged(stack)) return;

        if (player.tickCount % 8 == 0) {
            if (consumeHealth(player,stack)) {
                player.stopUsingItem();
                return;
            }
            player.hurt(level.damageSources().magic(), 1);
            player.invulnerableTime = 0;
            addCharge(stack,1);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && !isCharged(stack)) setCharge(stack, TARGET_CHARGE);

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        float charge = getCharge(stack);

        if (isCharged(stack)) {
            tooltip.add(Component.translatable("tooltip." + MikpikMod.MODID + ".heart_ready"));
        } else {
            tooltip.add(Component.translatable("tooltip." + MikpikMod.MODID + ".heart_cant_kill"));
            tooltip.add(Component.translatable("tooltip." + MikpikMod.MODID + ".heart_charge", charge, TARGET_CHARGE));
        }
    }

    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (!item.getItem().is(ModItems.HEART.get())) return;

        if (isCharged(item.getItem())) {
            item.setNoGravity(true);
            item.setInvulnerable(true);

            Vec3 motion = item.getDeltaMovement();

            item.setDeltaMovement(
                    motion.x * 0.85,
                    motion.y * 0.85,
                    motion.z * 0.85
            );
        }
    }
}
