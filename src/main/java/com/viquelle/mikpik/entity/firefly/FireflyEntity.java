package com.viquelle.mikpik.entity.firefly;

import com.viquelle.mikpik.client.darknesscomputer.Darkness;
import com.viquelle.mikpik.registry.ModItems;
import com.viquelle.mikpik.registry.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class FireflyEntity extends Entity {
    public static final float SKY_BRIGHT_THRESHOLD = 1f;
    public static final float BLOCK_BRIGHT_THRESHOLD = 3f;

    private static final EntityDataAccessor<Boolean> DATA_DORMANT = SynchedEntityData.defineId(FireflyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int WAKE_UP_DELAY = 200;
    private int sleepTimer = 0;

    public FireflyEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DORMANT, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) clientTick();
        else serverTick();
    }

    public static boolean isDarkOnPos(Level level, Vec3 pos) {
        return level.isNight() &&
                Darkness.posSkyLight(pos, level, 0f) <= SKY_BRIGHT_THRESHOLD &&
                level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos)) <= BLOCK_BRIGHT_THRESHOLD;
    }
    private void serverTick() {
        boolean isDark = isDarkOnPos(level(), position());

        if (!isDark) {
            setDormant(true);
            sleepTimer = 0;
            return;
        }

        if (isDormant()) {
            sleepTimer++;

            if (sleepTimer >= WAKE_UP_DELAY) {
                sleepTimer = 0;
                setDormant(false);
            }
        }
    }

    private void clientTick() {
        if (isDormant()) return;

        if (tickCount % 15 == 0) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(
                        ModParticleTypes.FIREFLY.get(),
                        getX(),
                        getY() + 0.5,
                        getZ(),
                        i,
                        0,
                        0
                );
            }
        }
    }

    public boolean isDormant() {
        return entityData.get(DATA_DORMANT);
    }

    private void setDormant(boolean dormant) {
        entityData.set(DATA_DORMANT, dormant);
    }

    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide) return;

        setDormant(true);
        sleepTimer = 0;
    }

    @Override
    public boolean isPickable() {
        if (isDormant()) return false;

        if (level().isClientSide) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                boolean hasBottle =
                        mc.player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GLASS_BOTTLE) ||
                                mc.player.getItemInHand(InteractionHand.OFF_HAND).is(Items.GLASS_BOTTLE);

                return hasBottle;
            }
        }

        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isDormant()) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).is(ModItems.PIBBLE.get())) return InteractionResult.PASS;

        if (!level().isClientSide) {
            ItemStack result = new ItemStack(ModItems.PIBBLE.get());

            if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
            if (!player.getInventory().add(result)) player.drop(result, false);

            discard();
        }

        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_DORMANT, tag.getBoolean("Dormant"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Dormant", entityData.get(DATA_DORMANT));
    }
}