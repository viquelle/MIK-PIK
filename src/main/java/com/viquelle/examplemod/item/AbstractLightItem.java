package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.AbstractLight;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import java.util.List;

public abstract class AbstractLightItem extends Item {
    protected static final String TAG_ENABLED = "enabled";
    protected static final String TAG_COOLDOWN = "light_cooldown";

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
        if (!level.isClientSide) return;
        if (entity instanceof Player player) {
            if (isEnabled(stack) && !selected && player.getOffhandItem() != stack) {
                toggleTo(stack, false);
            }
        }

        CompoundTag tag = getTag(stack);
        int cd = tag.getInt(TAG_COOLDOWN);
        if (cd > 0) {
            tag.putInt(TAG_COOLDOWN, cd - 1);
            setTag(stack, tag);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = getTag(stack);

        if (tag.getInt(TAG_COOLDOWN) > 0) {
            return InteractionResultHolder.fail(stack);
        }

        boolean newState = !tag.getBoolean(TAG_ENABLED);
        tag.putBoolean(TAG_ENABLED, newState);
        tag.putInt(TAG_COOLDOWN, 15);

        setTag(stack, tag);

        if (level.isClientSide) {
            player.playSound(SoundEvents.FLINTANDSTEEL_USE, 1.0f, 1.2f);
//            player.displayClientMessage(Component.literal(newState ? "§aON" : "§cOFF"), true);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        MutableComponent statusLabel = Component.translatable("tooltip.examplemod.status").withStyle(ChatFormatting.GRAY);
        MutableComponent stateValue;

        if (isEnabled(stack)) {
            stateValue = Component.translatable("tooltip.examplemod.status.on").withStyle(ChatFormatting.GOLD);
        } else {
            stateValue = Component.translatable("tooltip.examplemod.status.off").withStyle(ChatFormatting.GRAY);
        }

        tooltipComponents.add(statusLabel.append(stateValue));
    }

    public abstract AbstractLight<?> createLight(Player player);
}