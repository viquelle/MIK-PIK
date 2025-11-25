package com.viquelle.examplemod.sanity;

import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.capability.ModDataAttachments;
import com.viquelle.examplemod.data.SanityData;
import com.viquelle.examplemod.network.SanityPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class SanityUtil {
    public static float getSanity(Player player) {
        ExampleMod.LOGGER.debug("[SANITY UTIL] Getting sanity for player {}", player.getName().toString());
        SanityData sanity = player.getData(ModDataAttachments.SANITY);
        if (sanity == null) {
            ExampleMod.LOGGER.error("[SANITY UTIL] Capability is null for player {}", player.getName().toString());
        }

        ExampleMod.LOGGER.debug("[SANITY UTIL] Returning sanity value: {}", sanity.getSanity());
        return sanity.getSanity();
    }

    public static void set(Player player, float value) {
        ExampleMod.LOGGER.info("[SanityUtil] Setting sanity for player: {} to value: {}", player.getName().getString(), value); // Используем info для set
        SanityData data = player.getData(ModDataAttachments.SANITY);
        if (data == null) {
            ExampleMod.LOGGER.error("[SanityUtil] Capability is null for player: {} during set operation!", player.getName().getString());
            return; // Выходим, если capability не найдена
        }
        float oldValue = data.getSanity(); // Логируем старое значение
        ExampleMod.LOGGER.debug("[SanityUtil] Old sanity value: {}", oldValue);
        data.setSanity(value);
        player.setData(ModDataAttachments.SANITY, data);
        float newValue = data.getSanity(); // Логируем новое значение после set
        ExampleMod.LOGGER.debug("[SanityUtil] New sanity value after set: {}", newValue);

        if (player instanceof ServerPlayer serverPlayer) {
            ExampleMod.LOGGER.debug("[SanityUtil] Sending sync packet for player: {}", serverPlayer.getName().getString());
            PacketDistributor.sendToPlayer(serverPlayer, new SanityPayload(newValue));
        } else {
            ExampleMod.LOGGER.warn("[SanityUtil] Player is not a ServerPlayer, cannot send packet. Player: {}", player.getName().getString());
        }
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SanityPayload(player.getData(ModDataAttachments.SANITY).getSanity()));
        }
    }

    public static void add(Player player, float delta) {
        set(player, getSanity(player) + delta);
    }

    public static void reduce(Player player, float delta) {
        set(player, getSanity(player) - delta);
    }
}
