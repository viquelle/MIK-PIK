package com.viquelle.examplemod.item.lightItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;

public abstract class AbstractLightItem extends Item {
    public static final String TAG_ENABLED = "enabled";
    public static final String TAG_FUEL = "fuel";


    public AbstractLightItem(Properties properties) {
        super(properties);
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_ENABLED);
    }

    public static void setEnabled(ItemStack stack, boolean value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putBoolean(TAG_ENABLED,value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setFuel(ItemStack stack, int value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putInt(TAG_FUEL, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getFuel(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_FUEL);
    }

    public static void consumeFuel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putInt(TAG_FUEL, Math.max(0,getFuel(stack) - 1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getFuel(stack) <= 0) {
            setEnabled(stack,false);
            return InteractionResultHolder.fail(stack);
        }

        setEnabled(stack, !isEnabled(stack));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide && !selected && !isEnabled(stack)) return;

        float fuel = getFuel(stack);
        if (fuel <= 0) {
            setEnabled(stack,false);
            return;
        }

        consumeFuel(stack);
    }

}
