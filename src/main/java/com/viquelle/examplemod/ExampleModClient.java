package com.viquelle.examplemod;

import com.viquelle.examplemod.client.ClientLightManager;
import com.viquelle.examplemod.darknesscomputer.Darkness;
import com.viquelle.examplemod.item.AbstractLightItem;
import com.viquelle.examplemod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import static net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE;

@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.LIGHTER.get(),
                    ResourceLocation.fromNamespaceAndPath("examplemod", "enabled"),
                    ((itemStack, clientLevel, livingEntity, i) -> {
                        return AbstractLightItem.isEnabled(itemStack) ? 1.0f : 0.0f;
                    }));
        });
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        ClientLightManager.tick(
                event.getPartialTick().getGameTimeDeltaPartialTick(false)
        );
    }


}
