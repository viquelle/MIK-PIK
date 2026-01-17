package com.viquelle.mikpik;

import com.viquelle.mikpik.client.ClientLightManager;
import com.viquelle.mikpik.item.AbstractLightItem;
import com.viquelle.mikpik.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MikpikMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class MikpikModClient {
    public MikpikModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MikpikMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        MikpikMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.LIGHTER.get(),
                    ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "enabled"),
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
