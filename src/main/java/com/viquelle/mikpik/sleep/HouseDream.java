package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HouseDream extends DreamInstance {
    private static final int AREA_WIDTH_CHUNKS = 5;
    private static final int AREA_DEPTH_CHUNKS = 5;

    private final List<BlockPos> requiredLogs = new ArrayList<>();

    public HouseDream(UUID id) {
        super(id);
    }

    @Override
    protected DreamPlayerState getInitialPlayerState(ServerPlayer player) {
        return DreamPlayerState.defaults();
    }

    @Override
    public void addPlayer(ServerPlayer player) {
        super.addPlayer(player);
        player.getInventory().add(new ItemStack(ModItems.EPHEMERAL_AXE.get()));
        player.getInventory().add(new ItemStack(Blocks.OAK_LOG, 5));
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
        BlockPos center = area().center();
        buildGround(level, center);
        buildHouse(level, center);
        buildForest(level, center);

        if (level.isNight()) {
            placeTorches(level, center);
        }
    }

    @Override
    public void tick(ServerLevel level) {
        if (!hasPlayers()) return;

        for (BlockPos pos : requiredLogs) {
            if (!level.getBlockState(pos).isAir()) continue;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    3,
                    0.2,
                    0.2,
                    0.2,
                    0.01
            );
        }
    }

    private void buildGround(ServerLevel level, BlockPos center) {
        BlockPos min = area().min();
        BlockPos max = area().max();
        int groundY = center.getY();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                level.setBlockAndUpdate(
                        new BlockPos(x, groundY, z),
                        Blocks.GRASS_BLOCK.defaultBlockState()
                );

                level.setBlockAndUpdate(
                        new BlockPos(x, groundY - 1, z),
                        Blocks.DIRT.defaultBlockState()
                );

                level.setBlockAndUpdate(
                        new BlockPos(x, groundY - 2, z),
                        Blocks.DIRT.defaultBlockState()
                );
            }
        }
    }

    private void buildHouse(ServerLevel level, BlockPos center) {
        int x0 = center.getX() - 3;
        int z0 = center.getZ() - 3;
        int y = center.getY() + 1;

        for (int x = x0; x <= x0 + 6; x++) {
            for (int z = z0; z <= z0 + 6; z++) {
                level.setBlockAndUpdate(
                        new BlockPos(x, y, z),
                        Blocks.OAK_PLANKS.defaultBlockState()
                );
            }
        }

        for (int x = x0; x <= x0 + 6; x++) {
            for (int z = z0; z <= z0 + 6; z++) {
                if (x != x0 && x != x0 + 6 && z != z0 && z != z0 + 6) continue;

                for (int yy = y + 1; yy <= y + 3; yy++) {
                    level.setBlockAndUpdate(
                            new BlockPos(x, yy, z),
                            Blocks.OAK_PLANKS.defaultBlockState()
                    );
                }
            }
        }

        buildSupport(level, x0, y, z0);
        buildSupport(level, x0 + 6, y, z0);
        buildSupport(level, x0, y, z0 + 6);
        buildSupport(level, x0 + 6, y, z0 + 6);

        for (int x = x0; x <= x0 + 6; x++) {
            level.setBlockAndUpdate(
                    new BlockPos(x, y + 4, z0),
                    Blocks.OAK_LOG.defaultBlockState()
            );

            level.setBlockAndUpdate(
                    new BlockPos(x, y + 4, z0 + 6),
                    Blocks.OAK_LOG.defaultBlockState()
            );
        }

        for (int z = z0; z <= z0 + 6; z++) {
            level.setBlockAndUpdate(
                    new BlockPos(x0, y + 4, z),
                    Blocks.OAK_LOG.defaultBlockState()
            );

            level.setBlockAndUpdate(
                    new BlockPos(x0 + 6, y + 4, z),
                    Blocks.OAK_LOG.defaultBlockState()
            );
        }

        BlockPos doorPos = new BlockPos(x0 + 3, y + 1, z0);

        level.setBlockAndUpdate(
                doorPos,
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, Direction.NORTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                        .setValue(DoorBlock.OPEN, false)
        );

        level.setBlockAndUpdate(
                doorPos.above(),
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, Direction.NORTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                        .setValue(DoorBlock.OPEN, false)
        );

        requiredLogs.clear();

        requiredLogs.add(new BlockPos(x0 + 2, y + 1, z0));
        requiredLogs.add(new BlockPos(x0 + 4, y + 1, z0));
        requiredLogs.add(new BlockPos(x0 + 2, y + 2, z0));
        requiredLogs.add(new BlockPos(x0 + 4, y + 2, z0));
        requiredLogs.add(new BlockPos(x0 + 3, y + 3, z0));

        for (BlockPos pos : requiredLogs) {
            level.setBlockAndUpdate(
                    pos,
                    Blocks.AIR.defaultBlockState()
            );
        }
    }

    private void buildSupport(ServerLevel level, int x, int y, int z) {
        for (int yy = 0; yy <= 3; yy++) {
            level.setBlockAndUpdate(
                    new BlockPos(x, y + yy, z),
                    Blocks.OAK_LOG.defaultBlockState()
            );
        }
    }

    private void buildForest(ServerLevel level, BlockPos center) {
        RandomSource random = RandomSource.create(id().getMostSignificantBits());

        for (int i = 0; i < 35; i++) {
            int x = center.getX() + random.nextInt(70) - 35;
            int z = center.getZ() + random.nextInt(70) - 35;

            if (Math.abs(x - center.getX()) < 12 && Math.abs(z - center.getZ()) < 12) continue;

            int height = 8 + random.nextInt(5);
            buildOakTree(
                    level,
                    new BlockPos(x, center.getY() + 1, z),
                    height
            );
        }
    }

    private void buildOakTree(ServerLevel level, BlockPos pos, int height) {
        for (int y = 0; y < height; y++) {
            level.setBlockAndUpdate(
                    pos.above(y),
                    Blocks.OAK_LOG.defaultBlockState()
            );
        }

        int top = pos.getY() + height;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;

                level.setBlockAndUpdate(
                        new BlockPos(pos.getX() + x, top, pos.getZ() + z),
                        Blocks.OAK_LEAVES.defaultBlockState()
                );
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlockAndUpdate(
                        new BlockPos(pos.getX() + x, top + 1, pos.getZ() + z),
                        Blocks.OAK_LEAVES.defaultBlockState()
                );
            }
        }
    }

    private void placeTorches(ServerLevel level, BlockPos center) {
        int y = center.getY() + 1;

        for (int x = -5; x <= 5; x += 2) {
            level.setBlockAndUpdate(
                    new BlockPos(center.getX() + x, y, center.getZ() - 5),
                    Blocks.TORCH.defaultBlockState()
            );

            level.setBlockAndUpdate(
                    new BlockPos(center.getX() + x, y, center.getZ() + 5),
                    Blocks.TORCH.defaultBlockState()
            );
        }

        for (int z = -3; z <= 3; z += 2) {
            level.setBlockAndUpdate(
                    new BlockPos(center.getX() - 5, y, center.getZ() + z),
                    Blocks.TORCH.defaultBlockState()
            );

            level.setBlockAndUpdate(
                    new BlockPos(center.getX() + 5, y, center.getZ() + z),
                    Blocks.TORCH.defaultBlockState()
            );
        }
    }

    @Override
    public BlockPos spawnPosition(ServerPlayer player) {
        BlockPos center = area().center();
        return new BlockPos(
                center.getX(),
                center.getY() + 1,
                center.getZ() - 9
        );
    }

    @Override
    public float spawnYaw() {
        return 0.0F;
    }

    @Override
    public float spawnPitch() {
        return 0.0f;
    }

    @Override
    public boolean canConnect(ServerPlayer player) {
        return false;
    }

    public void onBlockPlaced(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!requiredLogs.contains(pos)) return;
        if (!isHouseComplete(level)) return;

        openDoor(level);
    }

    public void onBlockBroken(ServerLevel level, ServerPlayer player, BlockPos pos) {
    }

    private boolean isHouseComplete(ServerLevel level) {
        for (BlockPos requiredLog : requiredLogs) {
            if (level.getBlockState(requiredLog).isAir()) return false;
        }

        return true;
    }

    private void openDoor(ServerLevel level) {
        BlockPos doorPos = new BlockPos(
                area().center().getX(),
                area().center().getY() + 2,
                area().center().getZ() - 3
        );

        level.setBlockAndUpdate(doorPos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(doorPos.above(), Blocks.AIR.defaultBlockState());
    }
}