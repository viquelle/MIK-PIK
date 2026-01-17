package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MikpikMod.MODID);

    public static final DeferredItem<Item> FLASHLIGHT = ITEMS.register(FlashlightItem.ITEM_NAME,
            () -> new FlashlightItem(new Item.Properties()));

    public static final DeferredItem<Item> LIGHTER = ITEMS.register(LighterItem.ITEM_NAME,
            () -> new LighterItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
