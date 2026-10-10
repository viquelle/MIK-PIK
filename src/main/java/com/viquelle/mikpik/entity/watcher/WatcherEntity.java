package com.viquelle.mikpik.entity.watcher;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import com.viquelle.mikpik.light.ClientLightManager;

public class WatcherEntity extends Entity {
    private int lifeTime = 0;
    private int maxLifeTime;
    private int animationTimer = 0;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.maxLifeTime = 600 + level.random.nextInt(200);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) return;

        lifeTime++;
        animationTimer = (animationTimer + 1) % 200;

        if (lifeTime >= maxLifeTime) {
            discard();
        }
    }

    public int getBlinkFrame() {
        if (animationTimer < 160) {
            return 0;
        } else if (animationTimer < 162) {
            return 1;
        } else if (animationTimer < 164) {
            return 2;
        } else if (animationTimer < 166) {
            return 3;
        } else {
            return 0;
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

}