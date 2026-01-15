package com.viquelle.examplemod;

import com.viquelle.examplemod.datagen.ModLanguageProvider;
import com.viquelle.examplemod.datagen.ModRecipeProvider;
import com.viquelle.examplemod.item.AbstractLightItem;
import com.viquelle.examplemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean IsDebugEnabled = true;
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        ModItems.register(modEventBus);
        ModCreativeTabs.TAB.register(modEventBus);
        modEventBus.addListener(this::gatherData);
        modContainer.registerConfig(ModConfig.Type.COMMON, com.viquelle.examplemod.datagen.ModConfig.SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FLASHLIGHT);
            event.accept(ModItems.LIGHTER);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onItemTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            if (stack.getItem() instanceof AbstractLightItem && AbstractLightItem.isEnabled(stack)) {
                AbstractLightItem.toggleTo(stack,false);
            }
        }
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, ExampleMod.MODID,"ru_ru"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, ExampleMod.MODID,"en_us"));
    }
}
