package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.network.PayloadHandler;
import com.viquelle.mikpik.network.payload.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SanitySyncPayload.TYPE, SanitySyncPayload.STREAM_CODEC, PayloadHandler::handleDataOnNetwork);
        registrar.playToServer(HeartReviveRequestPayload.TYPE, HeartReviveRequestPayload.STREAM_CODEC, PayloadHandler::handleHeartReviveRequest);
        registrar.playToServer(PushItemPayload.TYPE, PushItemPayload.STREAM_CODEC, PayloadHandler::handePushRequest);
        registrar.playToClient(ReviveSuccessPayload.TYPE, ReviveSuccessPayload.STREAM_CODEC, PayloadHandler::handleReviveResult);
        registrar.playToClient(GhostStatePayload.TYPE, GhostStatePayload.STREAM_CODEC, PayloadHandler::handleGhostState);
        registrar.playToServer(GhostRespawnRequest.TYPE, GhostRespawnRequest.STREAM_CODEC, PayloadHandler::handleGhostRespawnRequest);
        registrar.playToServer(DynamicBrightPayload.TYPE, DynamicBrightPayload.STREAM_CODEC, PayloadHandler::handleDynamicBright);
        registrar.playToClient(GameplayModePayload.TYPE, GameplayModePayload.STREAM_CODEC, PayloadHandler::handleGameplayMode);
        registrar.playToServer(CampfireCookRequestPayload.TYPE, CampfireCookRequestPayload.STREAM_CODEC, PayloadHandler::handleCookRequest);
    }


}
