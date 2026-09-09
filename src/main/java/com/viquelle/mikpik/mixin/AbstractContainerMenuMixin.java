package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.inventory.AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostNoInventory(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (GhostManager.isGhost(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doClick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mikpik$handleSpoilMerge(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        // Работаем ТОЛЬКО с обычным кликом (ЛКМ/ПКМ), игнорируем Shift, дроп и т.д.
        if (clickType != ClickType.PICKUP || slotId < 0) {
            return;
        }

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Slot slot = menu.getSlot(slotId);
        ItemStack slotStack = slot.getItem();
        ItemStack carriedStack = menu.getCarried();

        // Если слот пустой, курсор пустой или это разные типы предметов — пусть работает ванильная логика
        if (slotStack.isEmpty() || carriedStack.isEmpty() || !slotStack.is(carriedStack.getItem())) return;
        if (slotStack.has(ModDataComponents.SPOIL_TIME_REMAINING.get()) && carriedStack.has(ModDataComponents.SPOIL_TIME_REMAINING.get())) {
            int maxStack = slot.getMaxStackSize(carriedStack);
            // Сколько предметов пытаемся переложить (ЛКМ (button 0) = все, ПКМ (button 1) = 1)
            int amountToMove = (button == 0) ? carriedStack.getCount() : 1;
            int canMove = Math.min(amountToMove, maxStack - slotStack.getCount());

            if (canMove > 0) {
                float time1 = carriedStack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), 0f);
                float time2 = slotStack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), 0f);

                float totalUnits = (time1 * carriedStack.getCount()) + (time2 * slotStack.getCount());
                int newSlotCount = slotStack.getCount() + canMove;
                float avgTime = totalUnits / newSlotCount;

                slotStack.setCount(newSlotCount);
                slotStack.set(ModDataComponents.SPOIL_TIME_REMAINING.get(), avgTime);
                // Уменьшаем стак на курсоре
                carriedStack.shrink(canMove);
                menu.setCarried(carriedStack);

                // ОТМЕНЯЕМ ванильную логику, потому что свапнет
                ci.cancel();
            }
        }
    }
}
