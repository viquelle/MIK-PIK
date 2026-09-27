package com.viquelle.mikpik;

import com.viquelle.mikpik.registry.ModItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MikpikMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            TAB.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MikpikMod.MODID))
                    .icon(() -> new ItemStack(ModItems.WRAPPER.get()))
                    .displayItems((parameters, output) -> {
                        ModItems.ITEMS.getEntries().forEach(item -> {
                            output.accept(item.get());
                        });
                    })
                    .build()
            );


}