package com.viquelle.mikpik.entity.firefly;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class FireflySpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FireflySpawner.class);
    private static final int SPAWN_CHANCE = 15;
    private static final int MAX_COUNT = 5;
    private static final int CHUNK_RADIUS = 3;
    private static final double MIN_DISTANCE_TO_PLAYER_SQ = 24.0 * 24.0;

    public static void tick(ServerLevel level) {
        if (level.isDay()) return;

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        int chunkX = player.chunkPosition().x + level.random.nextInt(17) - 8;
        int chunkZ = player.chunkPosition().z + level.random.nextInt(17) - 8;

        int radiusBlocks = CHUNK_RADIUS * 16;
        AABB searchArea = new AABB(
                chunkX * 16 - radiusBlocks, level.getMinBuildHeight(), chunkZ * 16 - radiusBlocks,
                chunkX * 16 + 16 + radiusBlocks, level.getMaxBuildHeight(), chunkZ * 16 + 16 + radiusBlocks
        );
        if (level.getEntitiesOfClass(FireflyEntity.class, searchArea).size() >= MAX_COUNT) return;
        if (level.random.nextInt(100) >= SPAWN_CHANCE) return;

        int x = chunkX * 16 + level.random.nextInt(16);
        int z = chunkZ * 16 + level.random.nextInt(16);

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int targetY = level.getMinBuildHeight() - 1;

        for (int y = surfaceY; y > level.getMinBuildHeight(); y--) {
            BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(x, y, z);
            BlockState state = level.getBlockState(checkPos);

            if (state.isAir() ||
                    state.is(BlockTags.REPLACEABLE_BY_TREES) ||
                    state.is(BlockTags.WOOL_CARPETS) ||
                    state.is(BlockTags.SNOW) ||
                    state.is(BlockTags.LEAVES)) {
                continue;
            }

            if (state.isSolidRender(level, checkPos)) {
                targetY = y;
                break;
            } else if (state.getFluidState().is(FluidTags.WATER)) {
                BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos(x, y - 1, z);
                if (level.getBlockState(belowPos).isSolidRender(level, belowPos)) {
                    targetY = y;
                    break;
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        if (targetY == level.getMinBuildHeight() - 1) return;

        double entityY = targetY + 2.0;
        Vec3 pos = new Vec3(x + 0.5, entityY, z + 0.5);

        if (player.distanceToSqr(pos) < MIN_DISTANCE_TO_PLAYER_SQ) return;
        if (!isAreaPassable(level, x, entityY, z)) return;
        if (!FireflyEntity.isDarkOnPos(level, pos)) return;

        FireflyEntity firefly = ModEntities.FIREFLY.get().create(level);
        if (firefly != null) {
            firefly.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(firefly);
            LOGGER.info("Spawned Firefly at X={}, Y={}, Z={}", pos.x, pos.y, pos.z);
        } else {
            LOGGER.error("Failed to create FireflyEntity instance.");
        }
    }

    private static boolean isAreaPassable(Level level, int x, double entityY, int z) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        int startY = (int) Math.floor(entityY);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    checkPos.set(x + dx, startY + dy, z + dz);
                    BlockState state = level.getBlockState(checkPos);
                    if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.REPLACEABLE_BY_TREES)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (level.isDay()) return;
        if (level.getGameTime() % 20 != 0) return;
        tick(level);
    }
}