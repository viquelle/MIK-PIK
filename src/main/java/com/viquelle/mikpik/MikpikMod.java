package com.viquelle.mikpik;

import com.mojang.logging.LogUtils;
import com.viquelle.mikpik.registry.*;
import com.viquelle.mikpik.datagen.ModLanguageProvider;
import com.viquelle.mikpik.datagen.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MikpikMod.MODID)
public class MikpikMod {
    public static final String MODID = "mikpik";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean IsDebugEnabled = true;
    public MikpikMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
//        ModItems.register(modEventBus);
        ModCreativeTabs.TAB.register(modEventBus);
        modEventBus.addListener(this::gatherData);
        modContainer.registerConfig(ModConfig.Type.COMMON, com.viquelle.mikpik.datagen.ModConfig.SPEC);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
//            event.accept(ModItems.FLASHLIGHT);
//            event.accept(ModItems.LIGHTER);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }


    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MikpikMod.MODID,"ru_ru"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MikpikMod.MODID,"en_us"));
    }
}
