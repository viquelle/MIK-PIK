package com.viquelle.mikpik.item.items.wrapper;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class WrapperItem extends Item {
    public WrapperItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.has(DataComponents.CONTAINER)) {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

                for (ItemStack item : contents.nonEmptyItems()) {
                    if (!player.getInventory().add(item)) {
                        player.drop(item, false);
                    }
                }

                stack.remove(DataComponents.CONTAINER);
                return InteractionResultHolder.success(stack);

            } else {
                player.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new WrapperMenu(id, inv),
                        Component.translatable("gui." + MikpikMod.MODID + ".wrapper")
                ));
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
