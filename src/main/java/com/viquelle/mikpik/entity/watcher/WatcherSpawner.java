package com.viquelle.mikpik.entity.watcher;

import com.viquelle.mikpik.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.sanity.ClientSanityData;
import net.minecraft.world.phys.AABB;

import java.util.Random;

@EventBusSubscriber(Dist.CLIENT)
public class WatcherSpawner {
    private static int cooldown = 0;
    private static final Random random = new Random();

    private static final int MIN_COOLDOWN = 200;
    private static final double MIN_DISTANCE = 24.0;
    private static final double MAX_DISTANCE = 48.0;
    private static final double WATCHER_CHECK_RADIUS = 64.0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;

        Player player = mc.player;
        ClientLevel level = mc.level;

        updateExistingWatchers(player, level);

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        float sanity = ClientSanityData.get();
        if (sanity >= 70.0f) return;

        double spawnChance = getSpawnChance(sanity);
        if (random.nextDouble() > spawnChance) return;

        Vec3 spawnPos = findSpawnPosition(player, level);
        if (spawnPos != null) {
            WatcherEntity watcher = new WatcherEntity(
                    ModEntities.WATCHER.get(),
                    level
            );

            watcher.setPos(spawnPos);
            level.addEntity(watcher);

            cooldown = MIN_COOLDOWN;
        }
    }

    private static void updateExistingWatchers(Player player, ClientLevel level) {
        AABB searchArea = player.getBoundingBox().inflate(WATCHER_CHECK_RADIUS);

        for (WatcherEntity watcher : level.getEntitiesOfClass(WatcherEntity.class, searchArea)) {
            if (watcher.isRemoved()) continue;

            boolean isClose = watcher.distanceTo(player) < 16.0;
            boolean isLit = !ClientLightManager.isDarkOnPos(
                    watcher.blockPosition().getCenter(),
                    level,
                    1.0f
            );

            if (isClose || isLit) {
                watcher.discard();
            }
        }
    }

    private static double getSpawnChance(float sanity) {
        if (sanity >= 50.0f) return 0.05;
        if (sanity >= 30.0f) return 0.15;
        return 0.30;
    }

    private static Vec3 findSpawnPosition(Player player, Level level) {
        double playerX = player.getX();
        double playerZ = player.getZ();

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 30; attempt++) {
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);

            double angle = random.nextDouble() * Math.PI * 2.0;

            int x = (int) (playerX + distance * Math.cos(angle));
            int z = (int) (playerZ + distance * Math.sin(angle));

            for (int y = maxY; y >= minY; y--) {
                // Ground block.
                pos.set(x, y, z);

                BlockState state = level.getBlockState(pos);

                if (!state.isFaceSturdy(level, pos, Direction.UP)) continue;
                if (!level.getFluidState(pos).isEmpty()) continue;

                pos.move(Direction.UP);
                state = level.getBlockState(pos);

                if (!level.getFluidState(pos).isEmpty()) continue;
                if (!(state.isAir()
                        || state.canBeReplaced()
                        || state.is(BlockTags.LEAVES))) {
                    continue;
                }

                // Head space.
                pos.move(Direction.UP);
                state = level.getBlockState(pos);

                if (!level.getFluidState(pos).isEmpty()) continue;

                if (!(state.isAir()
                        || state.canBeReplaced()
                        || state.is(BlockTags.LEAVES))) {
                    continue;
                }

                Vec3 spawnPos = new Vec3(x + 0.5, y + 0.5 + 1.8, z + 0.5);

                if (!ClientLightManager.isDarkOnPos(
                        spawnPos,
                        level,
                        1.0f
                )) {
                    continue;
                }

                return spawnPos;
            }
        }

        return null;
    }
}
