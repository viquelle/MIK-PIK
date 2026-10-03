package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MikpikMod.MODID)
public final class SleepManager {
    private static final float ACCELERATED_DAY_TIME_PER_TICK = 5.0F;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();

        if (overworld == null) {
            return;
        }

        if (!overworld.isNight() && overworld.getDayTimePerTick() != -1.0f) {
            overworld.setDayTimePerTick(-1.0f);
            return;
        }

        int totalPlayers = 0;
        int activeSleeperes = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }

            totalPlayers++;

            if (player.isSleeping() && player.isSleepingLongEnough()) {
                if (!DreamManager.isDreaming(player)) {
                    DreamManager.enterDream(player);
                }
            }

            if (isCountedAsSleeping(player)) {
                activeSleeperes++;
            }
        }

        int requiredPercentage = overworld.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        int requiredPlayers = Math.max(1, (int) Math.ceil(totalPlayers * requiredPercentage / 100d));

        boolean enoughSleeping = totalPlayers > 0 && activeSleeperes >= requiredPlayers;
        float targetSpeed = enoughSleeping ? ACCELERATED_DAY_TIME_PER_TICK : -1.0f;

        if (overworld.getDayTimePerTick() != targetSpeed) {
            overworld.setDayTimePerTick(targetSpeed);
        }
    }

    public static boolean isCountedAsSleeping(ServerPlayer player) {
        return !player.isSpectator() && (player.isSleeping() || DreamManager.isDreaming(player));
    }

    public static int getSleepingPlayers(MinecraftServer server) {
        int count = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isCountedAsSleeping(player)) {
                count++;
            }
        }

        return count;
    }

    public static boolean areEnoughPlayersSleeping(MinecraftServer server, int requiredPercentage) {
        int totalPlayers = 0;
        int sleepingPlayers = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }

            totalPlayers++;

            if (isCountedAsSleeping(player)) {
                sleepingPlayers++;
            }
        }

        if (totalPlayers == 0) {
            return false;
        }

        int requiredPlayers = Math.max(
                1,
                (int) Math.ceil(totalPlayers * requiredPercentage / 100.0)
        );

        return sleepingPlayers >= requiredPlayers;
    }

    public static void announceGlobalSleepStatus(MinecraftServer server) {
        ServerLevel overworld = server.overworld();

        if (overworld == null || !overworld.canSleepThroughNights()) {
            return;
        }

        int requiredPercentage = overworld.getGameRules()
                .getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);

        int totalPlayers = getTotalPlayers(server);
        int sleepingPlayers = getSleepingPlayers(server);

        int requiredPlayers = Math.max(
                1,
                (int) Math.ceil(totalPlayers * requiredPercentage / 100.0)
        );

        Component message;

        if (sleepingPlayers >= requiredPlayers) {
            message = Component.translatable("sleep.skipping_night");
        } else {
            message = Component.translatable(
                    "sleep.players_sleeping",
                    sleepingPlayers,
                    requiredPlayers
            );
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.displayClientMessage(message, true);
        }
    }

    public static int getTotalPlayers(MinecraftServer server) {
        int count = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator()) {
                count++;
            }
        }

        return count;
    }

}