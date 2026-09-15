package com.viquelle.mikpik.item.items.wrapper;

import com.viquelle.mikpik.registry.ModItems;
import com.viquelle.mikpik.registry.ModMenuTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class WrapperMenu extends AbstractContainerMenu {
    private final Container wrapperContainer;

    public WrapperMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, ContainerLevelAccess.NULL);
    }

    public WrapperMenu(int containerId, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.WRAPPER_MENU.get(), containerId);

        this.wrapperContainer = new SimpleContainer(9) {
            @Override
            public void setChanged() { super.setChanged(); }
        };

        int sx = WrapperLayout.CENTER_START_X;
        int sy = WrapperLayout.CENTER_START_Y + WrapperLayout.TOP_MARGIN;
        int ss = WrapperLayout.SLOT_SIZE;

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(wrapperContainer, i, sx + i%3 * ss, sy + i/3 * ss));
        }

        int invStartY = WrapperLayout.INV_START_Y + WrapperLayout.TOP_MARGIN + 14;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(
                        new Slot(
                                playerInv,
                                col + row * 9 + 9,
                                WrapperLayout.BASE_X + col * ss,
                                invStartY + row * ss
                        )
                );
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(
                    new Slot(
                            playerInv,
                            col,
                            WrapperLayout.BASE_X + col * ss,
                            invStartY + 3 * ss + 4
                    )
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < 9; ++i) {
                ItemStack itemstack = this.wrapperContainer.removeItemNoUpdate(i);
                if (!itemstack.isEmpty() && !player.getInventory().add(itemstack)) {
                    player.drop(itemstack, false);
                }
            }
        }
    }

    public Container getWrapperContainer() {
        return this.wrapperContainer;
    }

    private void saveContentToWrapper(Player player) {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            items.set(i, this.wrapperContainer.getItem(i).copyAndClear());
        }

        boolean hasItems = false;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            return;
        }

        ItemContainerContents contents = ItemContainerContents.fromItems(items);

        if (player.getMainHandItem().is(ModItems.WRAPPER.get())) {
            player.getMainHandItem().set(DataComponents.CONTAINER, contents);
            return;
        } else if (player.getOffhandItem().is(ModItems.WRAPPER.get())) {
            player.getOffhandItem().set(DataComponents.CONTAINER, contents);
            return;
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(ModItems.WRAPPER.get())) {
                    stack.set(DataComponents.CONTAINER, contents);
                    return;
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = items.get(i);
            if (!itemstack.isEmpty() && !player.getInventory().add(itemstack)) {
                player.drop(itemstack, false);
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            saveContentToWrapper(player);
            player.closeContainer();
            return true;
        }
        return super.clickMenuButton(player, id);
    }
}