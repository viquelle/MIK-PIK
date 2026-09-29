package com.viquelle.mikpik.sleep;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

public final class HouseDream extends DreamInstance {

    private static final int AREA_WIDTH_CHUNKS = 5;
    private static final int AREA_DEPTH_CHUNKS = 5;

    public HouseDream(UUID id) {
        super(id);
    }

    @Override
    public int areaWidthChunks() {
        return AREA_WIDTH_CHUNKS;
    }

    @Override
    public int areaDepthChunks() {
        return AREA_DEPTH_CHUNKS;
    }

    @Override
    public void initialize(ServerLevel level) {
        BlockPos min = area().min();

        for (int x = min.getX(); x <= area().max().getX(); x++) {
            for (int z = min.getZ(); z <= area().max().getZ(); z++) {
                level.setBlockAndUpdate(
                        new BlockPos(x, 64, z),
                        Blocks.GRASS_BLOCK.defaultBlockState()
                );
            }
        }
    }

    @Override
    public BlockPos spawnPosition(ServerPlayer player) {
        BlockPos pos = area().center();
        return pos.atY(64);
    }

    @Override
    public boolean canConnect(ServerPlayer player) {
        return true;
    }
}
