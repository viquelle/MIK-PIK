package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MikpikMod.MODID)
public final class SleepManager {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            boolean sleeping = level.players().stream()
                            .anyMatch(ServerPlayer::isSleeping);
            if (level.getDayTimePerTick() != -1.0F && !sleeping) {
                level.setDayTimePerTick(-1.0F);
            }
        }
    }
}