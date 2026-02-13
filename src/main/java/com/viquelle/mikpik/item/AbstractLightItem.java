package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.client.light.AbstractLight;
import com.viquelle.mikpik.network.LightTogglePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public abstract class AbstractLightItem extends Item {
    protected static final String TAG_ENABLED = "enabled";

    public AbstractLightItem(Properties properties) {
        super(properties);
    }

    public abstract String getKey(Player player);

    public static boolean isEnabled(ItemStack stack) {
        return getTag(stack).getBoolean(TAG_ENABLED);
    }

    public static void toggle(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        tag.putBoolean(TAG_ENABLED, !tag.getBoolean(TAG_ENABLED));
        setTag(stack, tag);
    }

    public static void toggleTo(ItemStack stack, boolean value) {
        CompoundTag tag = getTag(stack);
        tag.putBoolean(TAG_ENABLED, value);
        setTag(stack, tag);
    }

    private static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;

        if (entity instanceof Player player) {
            boolean wasEnabled = isEnabled(stack);

            if (wasEnabled && !selected && player.getOffhandItem() != stack) {
                toggleTo(stack, false);

                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer,
                            new LightTogglePacket(slot, false));
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        MikpikMod.LOGGER.info("TRYING TO USE is client: {}", level.isClientSide);
        ItemStack stack = player.getItemInHand(hand);
        float pitch = 1.15f + level.random.nextFloat() * 0.1f;

        if (level.isClientSide) {
            player.getCooldowns().addCooldown(this, 15);
            player.playSound(SoundEvents.FLINTANDSTEEL_USE, 1.0f, pitch);
            return InteractionResultHolder.success(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        // Сервер делает всё
        CompoundTag tag = getTag(stack);
        boolean newState = !tag.getBoolean(TAG_ENABLED);
        tag.putBoolean(TAG_ENABLED, newState);
        setTag(stack, tag);

        player.getCooldowns().addCooldown(this, 15);

        int slot = getSlotForItem(player, stack);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new LightTogglePacket(slot, newState));
        }

        level.playSound(player, player.blockPosition(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                1.0f, pitch);

        return InteractionResultHolder.consume(stack);
    }

    private int getSlotForItem(Player player, ItemStack target) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == target) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        MutableComponent statusLabel = Component.translatable("tooltip.mikpik.status").withStyle(ChatFormatting.GRAY);
        MutableComponent stateValue;

        if (isEnabled(stack)) {
            stateValue = Component.translatable("tooltip.mikpik.status.on").withStyle(ChatFormatting.GOLD);
        } else {
            stateValue = Component.translatable("tooltip.mikpik.status.off").withStyle(ChatFormatting.GRAY);
        }

        tooltipComponents.add(statusLabel.append(stateValue));
    }

    public abstract AbstractLight<?> createLight(Player player);
}