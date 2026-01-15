package com.viquelle.examplemod;

import com.viquelle.examplemod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> MAIN_TAB =
            TAB.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ExampleMod.MODID))
                    .icon(() -> new ItemStack(ModItems.LIGHTER.get()))
                    .displayItems(((itemDisplayParameters, output) -> {
                        ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })).build()
            );
}
