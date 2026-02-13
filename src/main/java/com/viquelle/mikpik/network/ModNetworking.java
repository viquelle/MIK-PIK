package com.viquelle.mikpik.network;

import com.viquelle.mikpik.MikpikMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                LightTogglePacket.TYPE,
                LightTogglePacket.STREAM_CODEC,
                LightTogglePacket::handle
        );
    }
}
