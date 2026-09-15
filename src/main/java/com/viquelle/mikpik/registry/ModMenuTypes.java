package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.items.wrapper.WrapperMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MikpikMod.MODID);

    public static final Supplier<MenuType<WrapperMenu>> WRAPPER_MENU =
            MENUS.register("wrapper_menu", () -> new MenuType<>(WrapperMenu::new, FeatureFlags.VANILLA_SET));
}

