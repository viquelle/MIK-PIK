package com.viquelle.mikpik;

import com.viquelle.mikpik.block.meateffigy.MeatEffigyBlockRenderer;
import com.viquelle.mikpik.block.meateffigy.MeatEffigyModel;
import com.viquelle.mikpik.client.ClientHeartManager;
import com.viquelle.mikpik.client.coloredlights.ColorLightPreProcessor;
import com.viquelle.mikpik.client.coloredlights.ColorLightRenderer;
import com.viquelle.mikpik.entity.eye.EyeRenderer;
import com.viquelle.mikpik.entity.firefly.FirefliRenderer;
import com.viquelle.mikpik.entity.firefly.FireflyParticleProvider;
import com.viquelle.mikpik.entity.hand.HandRenderer;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowForearmModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowHandModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowPortalModel;
import com.viquelle.mikpik.entity.watcher.WatcherRenderer;
import com.viquelle.mikpik.item.items.LifeInjectorItem;
import com.viquelle.mikpik.item.items.Magnetlampe;
import com.viquelle.mikpik.item.items.wrapper.WrapperScreen;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.source.*;
import com.viquelle.mikpik.registry.*;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.LevelEvent;

@Mod(value = MikpikMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class MikpikModClient {
    public MikpikModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MikpikMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        MikpikMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        ClientLightManager.register(new TorchLightSource());
        ClientLightManager.register(new PlayerAmbientLightSource());
        ClientLightManager.register(new MagnetlampeLightSource());
        ClientLightManager.register(new GhostPlayerLightSource());
        ClientLightManager.register(new WatcherEntityLightSource());
        ClientLightManager.register(new FireflyLightSource());

        VeilEventPlatform.INSTANCE.onVeilAddShaderProcessors(((resourceProvider, registry) -> {
            registry.addPreprocessor(ColorLightPreProcessor.INSTANCE, true);
        }));

        ItemProperties.register(
                ModItems.MAGNETLAMPE.get(),
                ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "charged"),
                (stack, level, entity, seed) -> Magnetlampe.getPercent(stack, level) > 0.0f ? 1.0f : 0.0f
        );

        ItemProperties.register(
                ModItems.WRAPPER.get(),
                ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "wrapped"),
                (stack, level, entity, seed) -> stack.has(DataComponents.CONTAINER) ? 1 : 0
        );

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                LifeInjectorItem.ANIM_LAYER_ID,
                1500,
                player -> new PlayerAnimationController(player, (controller, state, animSetter) -> PlayState.STOP)
        );

    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ClientLightManager.clear();
        }
    }

    private static boolean enabled = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            ClientLightManager.tick(mc.level, mc.player, event.getPartialTick().getGameTimeDeltaPartialTick(true));
            SanityPostShaderHandler.init();
            if (!enabled) {
                enabled = true;
                var renderer = VeilRenderSystem.renderer();
                if (renderer != null) {
                    SanityPostShaderHandler.tick();
                }
            }
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            ColorLightRenderer.INSTANCE.tick(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShadowForearmModel.LAYER_LOCATION, ShadowForearmModel::createBodyLayer);
        event.registerLayerDefinition(ShadowHandModel.LAYER_LOCATION, ShadowHandModel::createBodyLayer);
        event.registerLayerDefinition(ShadowPortalModel.LAYER_LOCATION, ShadowPortalModel::createBodyLayer);
        event.registerLayerDefinition(MeatEffigyModel.LAYER_LOCATION, MeatEffigyModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FIREFLY.get(), FireflyParticleProvider::new);
    }

    @SubscribeEvent
    public static void onre(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WATCHER.get(), WatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.HAND.get(), HandRenderer::new);
        event.registerEntityRenderer(ModEntities.FIREFLY.get(), FirefliRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MEAT_EFFIGY.get(), MeatEffigyBlockRenderer::new);
        event.registerEntityRenderer(ModEntities.EYE.get(), EyeRenderer::new);
    }

    @SubscribeEvent
    public static void onRe(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WRAPPER_MENU.get(), WrapperScreen::new);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientHeartManager.clientTick();
    }
}