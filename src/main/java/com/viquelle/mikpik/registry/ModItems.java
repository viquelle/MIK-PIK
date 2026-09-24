package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.items.*;
import com.viquelle.mikpik.item.items.wrapper.WrapperItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MikpikMod.MODID);

    public static final Supplier<Item> PIBBLE =
            ITEMS.register("pibble", () -> new PibbleItem(new Item.Properties())
    );

    public static final Supplier<Item> PLUSHY =
            ITEMS.register("plushy", () -> new PlushyItem(new Item.Properties()));

    public static final Supplier<Item> FLOWER_CROWN =
            ITEMS.register("flower_crown", () -> new FlowerCrownItem(
                    ModArmorMaterials.FLOWER_CROWN,
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(20*60*16)
            ));

    public static final Supplier<Item> MAGNETLAMPE =
            ITEMS.register("magnetlampe", () -> new Magnetlampe(
                    new Item.Properties()
            ));

    public static final Supplier<Item> HEART =
            ITEMS.register("heart", () -> new HeartItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
            ));

    public static final Supplier<Item> LIFE_INJECTOR =
            ITEMS.register("life_injector", () -> new LifeInjectorItem(
                    new Item.Properties().stacksTo(1)
            ));

    public static final Supplier<Item> MEAT_EFFIGY =
            ITEMS.register(
                    "meat_effigy",
                    () -> new BlockItem(
                            ModBlocks.MEAT_EFFIGY.get(),
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    )
            );

    public static final Supplier<Item> HAM_BAT =
            ITEMS.register(
                    "ham_bat",
                    () -> new HamBatItem(new Item.Properties())
            );

    public static final Supplier<Item> WRAPPER =
            ITEMS.register(
                    "wrapper",
                    () -> new WrapperItem(new Item.Properties().stacksTo(4))
            );
}
