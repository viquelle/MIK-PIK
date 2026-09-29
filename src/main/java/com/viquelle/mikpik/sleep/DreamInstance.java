package com.viquelle.mikpik.sleep;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class DreamInstance {

    private final UUID id;
    private DreamArea area;

    private final Set<UUID> players = new HashSet<>();

    protected DreamInstance(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public DreamArea area() {
        if (area == null) {
            throw new IllegalStateException(
                    "Dream area has not been assigned yet: " + id
            );
        }

        return area;
    }

    public void assignArea(DreamArea area) {
        if (this.area != null) {
            throw new IllegalStateException(
                    "Dream area is already assigned: " + id
            );
        }

        this.area = area;
    }

    public Set<UUID> players() {
        return Set.copyOf(players);
    }

    public void addPlayer(UUID playerId) {
        players.add(playerId);
    }

    public void playerLeft(UUID playerId) {
        players.remove(playerId);
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    public abstract int areaWidthChunks();

    public abstract int areaDepthChunks();

    public abstract void initialize(ServerLevel level);

    public void cleanup(ServerLevel level) {
        if (area == null || level == null) {
            return;
        }

        BlockPos min = area.min();
        BlockPos max = area.max();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    level.setBlockAndUpdate(
                            new BlockPos(x, y, z),
                            Blocks.AIR.defaultBlockState()
                    );
                }
            }
        }
    }

    public abstract BlockPos spawnPosition(ServerPlayer player);

    public abstract boolean canConnect(ServerPlayer player);
}

