package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MikpikMod.MODID)
public final class DreamManager {

    private static final Map<UUID, DreamInstance> ACTIVE_DREAMS =
            new HashMap<>();

    private static final Map<UUID, DreamInstance> PLAYER_DREAMS =
            new HashMap<>();

    private static final Map<UUID, ServerLevel> DREAM_LEVELS =
            new HashMap<>();

    private static final int AREA_HEIGHT = 128;
    private static final int AREA_GAP_CHUNKS = 1;

    private DreamManager() {
    }

    public static void enterDream(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (PLAYER_DREAMS.containsKey(playerId)) return;

        DreamInstance dream = findConnectableDream(player);
        if (dream == null) {
            ServerLevel dreamLevel = chooseHouseDreamLevel(player);
            dream = createDream(dreamLevel);
        }

        dream.addPlayer(playerId);
        PLAYER_DREAMS.put(playerId, dream);
        teleportToDream(player, dream);

        System.out.println(
                "[MIK-PIK] "
                        + player.getGameProfile().getName()
                        + " entered dream "
                        + dream.id()
        );
    }

    private static DreamInstance findConnectableDream(ServerPlayer player) {
        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            if (dream.canConnect(player)) {
                return dream;
            }
        }
        return null;
    }

    private static ServerLevel chooseHouseDreamLevel(ServerPlayer player) {
        ServerLevel dayLevel = player.server.getLevel(DreamDimension.DAY);
        ServerLevel fullMoonLevel = player.server.getLevel(DreamDimension.NIGHT_FULL_MOON);

        if (dayLevel == null) {
            throw new IllegalStateException(
                    "Dream day dimension is not available: "
                            + DreamDimension.DAY.location()
            );
        }

        if (fullMoonLevel == null) {
            throw new IllegalStateException(
                    "Dream full moon dimension is not available: "
                            + DreamDimension.NIGHT_FULL_MOON.location()
            );
        }

        return player.getRandom().nextBoolean()
                ? dayLevel
                : fullMoonLevel;
    }

    private static DreamInstance createDream(ServerLevel dreamLevel) {
        DreamInstance dream = new HouseDream(UUID.randomUUID());

        DreamArea area = allocateArea(
                dreamLevel,
                dream.areaWidthChunks(),
                dream.areaDepthChunks()
        );

        dream.assignArea(area);
        dream.initialize(dreamLevel);

        ACTIVE_DREAMS.put(dream.id(), dream);
        DREAM_LEVELS.put(dream.id(), dreamLevel);

        return dream;
    }

    private static DreamArea allocateArea(ServerLevel dreamLevel, int widthChunks, int depthChunks) {
        int width = widthChunks * 16;
        int depth = depthChunks * 16;

        int stepX = width + AREA_GAP_CHUNKS * 16;
        int stepZ = depth + AREA_GAP_CHUNKS * 16;

        int index = 0;

        while (true) {
            int gridX = index % 16;
            int gridZ = index / 16;

            int minX = gridX * stepX;
            int minZ = gridZ * stepZ;

            DreamArea candidate = new DreamArea(
                    new BlockPos(minX, 0, minZ),
                    new BlockPos(
                            minX + width - 1,
                            AREA_HEIGHT - 1,
                            minZ + depth - 1
                    )
            );

            if (isAreaFree(dreamLevel, candidate)) {
                return candidate;
            }

            index++;
        }
    }

    private static boolean isAreaFree(ServerLevel dreamLevel, DreamArea candidate) {
        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            ServerLevel activeLevel =
                    DREAM_LEVELS.get(dream.id());

            if (activeLevel != dreamLevel) {
                continue;
            }

            if (intersects(candidate, dream.area())) {
                return false;
            }
        }

        return true;
    }

    private static boolean intersects(DreamArea first, DreamArea second) {
        return first.min().getX() <= second.max().getX()
                && first.max().getX() >= second.min().getX()
                && first.min().getY() <= second.max().getY()
                && first.max().getY() >= second.min().getY()
                && first.min().getZ() <= second.max().getZ()
                && first.max().getZ() >= second.min().getZ();
    }

    private static void teleportToDream(ServerPlayer player, DreamInstance dream) {
        ServerLevel dreamLevel = DREAM_LEVELS.get(dream.id());

        if (dreamLevel == null) {
            throw new IllegalStateException(
                    "Dream level is not registered: "
                            + dream.id()
            );
        }

        BlockPos spawn = dream.spawnPosition(player);

        player.teleportTo(
                dreamLevel,
                spawn.getX() + 0.5,
                spawn.getY() + 1.0,
                spawn.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );
    }

    public static void playerLeft(ServerPlayer player) {
        UUID playerId = player.getUUID();

        DreamInstance dream = PLAYER_DREAMS.remove(playerId);
        if (dream == null) return;

        dream.playerLeft(playerId);

        if (!dream.hasPlayers()) closeDream(dream);
    }

    private static void closeDream(DreamInstance dream) {
        ServerLevel dreamLevel = DREAM_LEVELS.remove(dream.id());
        if (dreamLevel != null) dream.cleanup(dreamLevel);

        ACTIVE_DREAMS.remove(dream.id());
    }

    public static boolean isDreaming(ServerPlayer player) {
        return PLAYER_DREAMS.containsKey(player.getUUID());
    }

    public static DreamInstance getDream(ServerPlayer player) {
        return PLAYER_DREAMS.get(player.getUUID());
    }

    public static void shutdown() {
        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            ServerLevel dreamLevel = DREAM_LEVELS.get(dream.id());

            if (dreamLevel != null) dream.cleanup(dreamLevel);
        }

        ACTIVE_DREAMS.clear();
        PLAYER_DREAMS.clear();
        DREAM_LEVELS.clear();
    }

    private static void configureLevel(ServerLevel level, long dayTime) {
        if (level == null) {
            MikpikMod.LOGGER.error("Dream level is null!");
            return;
        }

        level.getGameRules()
                .getRule(GameRules.RULE_DAYLIGHT)
                .set(true, level.getServer());

        MikpikMod.LOGGER.info(
                "BEFORE {}: dayTime={}, gamerule={}",
                level.dimension().location(),
                level.getDayTime(),
                level.getGameRules()
                        .getRule(GameRules.RULE_DAYLIGHT)
                        .get()
        );

        level.setDayTime(dayTime);

        level.getGameRules()
                .getRule(GameRules.RULE_DAYLIGHT)
                .set(false, level.getServer());

        MikpikMod.LOGGER.info(
                "AFTER {}: dayTime={}, gameRule={}",
                level.dimension().location(),
                level.getDayTime(),
                level.getGameRules()
                        .getRule(GameRules.RULE_DAYLIGHT)
                        .get()
        );
    }

    @SubscribeEvent
    public static void playerLeftEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) playerLeft(serverPlayer);
    }

    @SubscribeEvent
    public static void serverShutdownEvent(ServerStoppingEvent event) {
        shutdown();
    }
}