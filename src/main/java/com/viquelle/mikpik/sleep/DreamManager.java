package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MikpikMod.MODID)
public final class DreamManager {
    private static final Map<UUID, DreamInstance> ACTIVE_DREAMS = new HashMap<>();
    private static final Map<UUID, DreamInstance> PLAYER_DREAMS = new HashMap<>();
    private static final Map<UUID, ServerLevel> DREAM_LEVELS = new HashMap<>();
    private static final Map<UUID, SleepPosition> SLEEP_POSITIONS = new HashMap<>();

    private static final int AREA_HEIGHT = 128;
    private static final int AREA_GAP_CHUNKS = 1;

    private static final int BOUNDARY_CHECK_INTERVAL = 20;
    private static int boundaryCheckTicks = 0;

    private static final int SLEEP_PARTICLE_INTERVAL = 2;
    private static int sleepParticleTicks = 0;

    private DreamManager() {
    }

    public static void enterDream(ServerPlayer player) {
        UUID playerId = player.getUUID();

        MikpikMod.LOGGER.info(
                "Player {} is entering a dream. Current level={}, position=({}, {}, {}), rotation=({}, {})",
                player.getGameProfile().getName(),
                player.level().dimension().location(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );

        if (PLAYER_DREAMS.containsKey(playerId)) {
            MikpikMod.LOGGER.info(
                    "Player {} is already in dream {}. Enter cancelled.",
                    player.getGameProfile().getName(),
                    PLAYER_DREAMS.get(playerId).id()
            );
            return;
        }

        saveSleepPosition(player);

        DreamInstance dream = findConnectableDream(player);
        if (dream == null) {
            MikpikMod.LOGGER.info(
                    "No connectable dream found for player {}. Creating new dream.",
                    player.getGameProfile().getName()
            );

            ServerLevel dreamLevel = chooseHouseDreamLevel(player);
            dream = createDream(dreamLevel);
        } else {
            MikpikMod.LOGGER.info(
                    "Player {} connected to existing dream {}.",
                    player.getGameProfile().getName(),
                    dream.id()
            );
        }

        dream.addPlayer(player);
        PLAYER_DREAMS.put(playerId, dream);

        MikpikMod.LOGGER.info(
                "Player {} assigned to dream {}. Dream players={}",
                player.getGameProfile().getName(),
                dream.id(),
                dream.players().size()
        );

        teleportToDream(player, dream);

        MikpikMod.LOGGER.info(
                "Player {} entered dream {}.",
                player.getGameProfile().getName(),
                dream.id()
        );
    }

    private static void saveSleepPosition(ServerPlayer player) {
        UUID playerId = player.getUUID();

        SleepPosition position = new SleepPosition(
                player.serverLevel(),
                player.blockPosition(),
                player.getYRot(),
                player.getXRot()
        );

        SLEEP_POSITIONS.put(playerId, position);

        MikpikMod.LOGGER.info(
                "Saved sleep position for player {}: level={}, position={}, rotation=({}, {})",
                player.getGameProfile().getName(),
                position.level().dimension().location(),
                position.position(),
                position.yRot(),
                position.xRot()
        );
    }

    private static DreamInstance findConnectableDream(ServerPlayer player) {
        MikpikMod.LOGGER.info(
                "Searching for connectable dream for player {}. Active dreams={}",
                player.getGameProfile().getName(),
                ACTIVE_DREAMS.size()
        );

        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            MikpikMod.LOGGER.info(
                    "Checking dream {} for player {}. Can connect={}",
                    dream.id(),
                    player.getGameProfile().getName(),
                    dream.canConnect(player)
            );

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

        ServerLevel selectedLevel = player.getRandom().nextBoolean()
                ? dayLevel
                : fullMoonLevel;

        MikpikMod.LOGGER.info(
                "Selected dream level {} for player {}.",
                selectedLevel.dimension().location(),
                player.getGameProfile().getName()
        );

        return selectedLevel;
    }

    private static DreamInstance createDream(ServerLevel dreamLevel) {
        MikpikMod.LOGGER.info(
                "Creating new HouseDream in level {}.",
                dreamLevel.dimension().location()
        );

        DreamInstance dream = new HouseDream(UUID.randomUUID());

        DreamArea area = allocateArea(
                dreamLevel,
                dream.areaWidthChunks(),
                dream.areaDepthChunks()
        );

        MikpikMod.LOGGER.info(
                "Allocated dream {} area: min={}, max={}",
                dream.id(),
                area.min(),
                area.max()
        );

        dream.assignArea(area);

        MikpikMod.LOGGER.info(
                "Initializing dream {}.",
                dream.id()
        );

        dream.initialize(dreamLevel);

        ACTIVE_DREAMS.put(dream.id(), dream);
        DREAM_LEVELS.put(dream.id(), dreamLevel);

        MikpikMod.LOGGER.info(
                "Dream {} created successfully. Active dreams={}",
                dream.id(),
                ACTIVE_DREAMS.size()
        );

        return dream;
    }

    private static DreamArea allocateArea(ServerLevel dreamLevel, int widthChunks, int depthChunks) {
        MikpikMod.LOGGER.info(
                "Allocating dream area in level {}. Size={}x{} chunks.",
                dreamLevel.dimension().location(),
                widthChunks,
                depthChunks
        );

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
                MikpikMod.LOGGER.info(
                        "Found free dream area in level {}: min={}, max={}, index={}",
                        dreamLevel.dimension().location(),
                        candidate.min(),
                        candidate.max(),
                        index
                );

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
                MikpikMod.LOGGER.info(
                        "Candidate area intersects dream {}. Candidate min={}, max={}, existing min={}, max={}",
                        dream.id(),
                        candidate.min(),
                        candidate.max(),
                        dream.area().min(),
                        dream.area().max()
                );

                return false;
            }
        }

        return true;
    }

    private static boolean intersects(DreamArea first, DreamArea second) {
        boolean result = first.min().getX() <= second.max().getX()
                && first.max().getX() >= second.min().getX()
                && first.min().getY() <= second.max().getY()
                && first.max().getY() >= second.min().getY()
                && first.min().getZ() <= second.max().getZ()
                && first.max().getZ() >= second.min().getZ();

        MikpikMod.LOGGER.info(
                "Checking dream area intersection: first min={}, max={}, second min={}, max={}, result={}",
                first.min(),
                first.max(),
                second.min(),
                second.max(),
                result
        );

        return result;
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

        MikpikMod.LOGGER.info(
                "Teleporting player {} to dream {}. Target level={}, position=({}, {}, {})",
                player.getGameProfile().getName(),
                dream.id(),
                dreamLevel.dimension().location(),
                spawn.getX() + 0.5,
                spawn.getY() + 1.0,
                spawn.getZ() + 0.5
        );

        player.teleportTo(
                dreamLevel,
                spawn.getX() + 0.5,
                spawn.getY() + 1.0,
                spawn.getZ() + 0.5,
                dream.spawnYaw(),
                dream.spawnPitch()
        );
    }

    private static void returnToSleepPosition(ServerPlayer player, SleepPosition sleepPosition) {
        player.resetFallDistance();

        ServerLevel level = sleepPosition.level();
        BlockPos origin = sleepPosition.position();

        BlockPos[] candidates = {
                origin.north(),
                origin.south(),
                origin.west(),
                origin.east(),
                origin.north().west(),
                origin.north().east(),
                origin.south().west(),
                origin.south().east(),
                origin
        };

        for (BlockPos candidate : candidates) {
            Vec3 position = new Vec3(
                    candidate.getX() + 0.5,
                    candidate.getY() + 1.0,
                    candidate.getZ() + 0.5
            );

            AABB boundingBox = player.getBoundingBox().move(
                    position.x - player.getX(),
                    position.y - player.getY(),
                    position.z - player.getZ()
            );

            if (level.noCollision(player, boundingBox)) {
                MikpikMod.LOGGER.info(
                        "Found safe return position for player {}: level={}, position={}",
                        player.getGameProfile().getName(),
                        level.dimension().location(),
                        position
                );

                player.teleportTo(
                        level,
                        position.x,
                        position.y,
                        position.z,
                        sleepPosition.yRot(),
                        sleepPosition.xRot()
                );

                return;
            }
        }

        Vec3 fallback = new Vec3(
                origin.getX() + 0.5,
                origin.getY() + 1.0,
                origin.getZ() + 0.5
        );

        MikpikMod.LOGGER.warn(
                "No safe return position found for player {}. Using fallback position: level={}, position={}",
                player.getGameProfile().getName(),
                level.dimension().location(),
                fallback
        );


        player.teleportTo(
                level,
                fallback.x,
                fallback.y,
                fallback.z,
                sleepPosition.yRot(),
                sleepPosition.xRot()
        );
    }

    public static void playerLeft(ServerPlayer player) {
        UUID playerId = player.getUUID();

        MikpikMod.LOGGER.info(
                "Player {} is leaving dream. Current level={}, position=({}, {}, {})",
                player.getGameProfile().getName(),
                player.level().dimension().location(),
                player.getX(),
                player.getY(),
                player.getZ()
        );

        DreamInstance dream = PLAYER_DREAMS.remove(playerId);

        if (dream == null) {
            MikpikMod.LOGGER.info(
                    "Player {} is not registered in a dream. Nothing to do.",
                    player.getGameProfile().getName()
            );
            return;
        }

        MikpikMod.LOGGER.info(
                "Removing player {} from dream {}.",
                player.getGameProfile().getName(),
                dream.id()
        );

        dream.playerLeft(player);

        SleepPosition sleepPosition = SLEEP_POSITIONS.remove(playerId);

        if (sleepPosition != null) {
            returnToSleepPosition(player, sleepPosition);

            BlockState state = sleepPosition.level().getBlockState(sleepPosition.position());
            if (state.is(BlockTags.BEDS) && state.getValue(BedBlock.OCCUPIED)) {
                sleepPosition.level().setBlock(
                        sleepPosition.position(),
                        state.setValue(BedBlock.OCCUPIED, false),
                        3
                );
            }

            MikpikMod.LOGGER.info(
                    "Removed saved sleep position for player {}: level={}, position={}",
                    player.getGameProfile().getName(),
                    sleepPosition.level().dimension().location(),
                    sleepPosition.position()
            );
        } else {
            MikpikMod.LOGGER.info(
                    "No saved sleep position found for player {}.",
                    player.getGameProfile().getName()
            );
        }

        if (!dream.hasPlayers()) {
            MikpikMod.LOGGER.info(
                    "Dream {} has no players left. Closing dream.",
                    dream.id()
            );

            closeDream(dream);
        } else {
            MikpikMod.LOGGER.info(
                    "Dream {} still has {} player(s).",
                    dream.id(),
                    dream.players().size()
            );
        }
    }

    private static void closeDream(DreamInstance dream) {
        MikpikMod.LOGGER.info(
                "Closing dream {}.",
                dream.id()
        );

        ServerLevel dreamLevel = DREAM_LEVELS.remove(dream.id());

        if (dreamLevel != null) {
            MikpikMod.LOGGER.info(
                    "Cleaning up dream {} in level {}.",
                    dream.id(),
                    dreamLevel.dimension().location()
            );

            dream.cleanup(dreamLevel);
        }

        ACTIVE_DREAMS.remove(dream.id());

        MikpikMod.LOGGER.info(
                "Dream {} closed. Active dreams={}",
                dream.id(),
                ACTIVE_DREAMS.size()
        );
    }

    public static boolean isDreaming(ServerPlayer player) {
        return PLAYER_DREAMS.containsKey(player.getUUID());
    }

    public static DreamInstance getDream(ServerPlayer player) {
        return PLAYER_DREAMS.get(player.getUUID());
    }

    public static void shutdown() {
        MikpikMod.LOGGER.info(
                "Shutting down DreamManager. Active dreams={}, players={}, saved sleep positions={}",
                ACTIVE_DREAMS.size(),
                PLAYER_DREAMS.size(),
                SLEEP_POSITIONS.size()
        );

        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            ServerLevel dreamLevel = DREAM_LEVELS.get(dream.id());

            if (dreamLevel != null) {
                MikpikMod.LOGGER.info(
                        "Cleaning up dream {} during shutdown.",
                        dream.id()
                );

                dream.cleanup(dreamLevel);
            }
        }

        ACTIVE_DREAMS.clear();
        PLAYER_DREAMS.clear();
        DREAM_LEVELS.clear();
        SLEEP_POSITIONS.clear();

        MikpikMod.LOGGER.info(
                "DreamManager shutdown complete."
        );
    }

    @SubscribeEvent
    public static void playerLeftEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        MikpikMod.LOGGER.info(
                "PlayerLoggedOutEvent received for {}.",
                player.getGameProfile().getName()
        );

        if (player instanceof ServerPlayer serverPlayer) {
            playerLeft(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void serverShutdownEvent(ServerStoppingEvent event) {
        MikpikMod.LOGGER.info(
                "ServerStoppingEvent received."
        );

        shutdown();
    }

    private static void checkDreamBoundaries() {
        if (PLAYER_DREAMS.isEmpty()) {
            return;
        }

        MikpikMod.LOGGER.info(
                "Checking dream boundaries. Players in dreams={}",
                PLAYER_DREAMS.size()
        );

        Map<UUID, DreamInstance> playersSnapshot =
                new HashMap<>(PLAYER_DREAMS);

        for (Map.Entry<UUID, DreamInstance> entry : playersSnapshot.entrySet()) {
            UUID playerId = entry.getKey();
            DreamInstance dream = entry.getValue();

            ServerLevel dreamLevel = DREAM_LEVELS.get(dream.id());

            if (dreamLevel == null) {
                MikpikMod.LOGGER.warn(
                        "Dream level is missing for dream {}. Skipping player {}.",
                        dream.id(),
                        playerId
                );
                continue;
            }

            ServerPlayer player = dreamLevel.getServer().getPlayerList().getPlayer(playerId);

            if (player == null) {
                MikpikMod.LOGGER.warn(
                        "Player {} is registered in dream {} but could not be found on server.",
                        playerId,
                        dream.id()
                );
                continue;
            }

            boolean insideDream = isInsideDream(player, dream, dreamLevel);

            MikpikMod.LOGGER.info(
                    "Boundary check for player {} in dream {}: inside={}",
                    player.getGameProfile().getName(),
                    dream.id(),
                    insideDream
            );

            if (!insideDream) {
                MikpikMod.LOGGER.info(
                        "Player {} left dream {} boundary. Removing player from dream.",
                        player.getGameProfile().getName(),
                        dream.id()
                );

                playerLeft(player);
            }
        }
    }

    private static boolean isInsideDream(ServerPlayer player, DreamInstance dream, ServerLevel dreamLevel) {
        if (player.level() != dreamLevel) {
            MikpikMod.LOGGER.info(
                    "Player {} is in wrong level. Expected={}, actual={}",
                    player.getGameProfile().getName(),
                    dreamLevel.dimension().location(),
                    player.level().dimension().location()
            );

            return false;
        }

        BlockPos position = player.blockPosition();
        DreamArea area = dream.area();

        boolean inside = position.getX() >= area.min().getX()
                && position.getX() <= area.max().getX()
                && position.getY() >= area.min().getY()
                && position.getY() <= area.max().getY()
                && position.getZ() >= area.min().getZ()
                && position.getZ() <= area.max().getZ();

        MikpikMod.LOGGER.info(
                "Checking player {} position={} against dream {} area min={}, max={}, inside={}",
                player.getGameProfile().getName(),
                position,
                dream.id(),
                area.min(),
                area.max(),
                inside
        );

        return inside;
    }

    private static void spawnSleepParticles() {
        for (Map.Entry<UUID, SleepPosition> entry : SLEEP_POSITIONS.entrySet()) {
            UUID playerId = entry.getKey();

            if (!PLAYER_DREAMS.containsKey(playerId)) {
                continue;
            }

            SleepPosition sleepPosition = entry.getValue();
            ServerLevel level = sleepPosition.level();
            BlockPos position = sleepPosition.position();

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    position.getX() + 0.5,
                    position.getY() + 0.8,
                    position.getZ() + 0.5,
                    2,
                    0.25,
                    0.1,
                    0.4,
                    0.006
            );
        }
    }

    @SubscribeEvent
    public static void blockPlacedEvent(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DreamInstance dream = PLAYER_DREAMS.get(player.getUUID());
        if (dream instanceof HouseDream houseDream) {
            houseDream.onBlockPlaced(player.serverLevel(), player, event.getPos());
        }
    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();

        DreamInstance dream = PLAYER_DREAMS.get(player.getUUID());
        if (dream instanceof HouseDream houseDream) {
            houseDream.onBlockBroken(player.serverLevel(), player, event.getPos());
        }
    }

    @SubscribeEvent
    public static void serverTickEvent(ServerTickEvent.Post event) {
        boundaryCheckTicks++;
        sleepParticleTicks++;

        if (boundaryCheckTicks >= BOUNDARY_CHECK_INTERVAL) {
            boundaryCheckTicks = 0;

            MikpikMod.LOGGER.info(
                    "Dream boundary check interval reached."
            );

            checkDreamBoundaries();
        }

        if (sleepParticleTicks >= SLEEP_PARTICLE_INTERVAL) {
            sleepParticleTicks = 0;
            spawnSleepParticles();
        }

        for (DreamInstance dream : ACTIVE_DREAMS.values()) {
            ServerLevel dreamLevel = DREAM_LEVELS.get(dream.id());
            if (dreamLevel != null && dream.hasPlayers()) dream.tick(dreamLevel);
        }
    }

    private record SleepPosition(
            ServerLevel level,
            BlockPos position,
            float yRot,
            float xRot
    ) {
    }
}