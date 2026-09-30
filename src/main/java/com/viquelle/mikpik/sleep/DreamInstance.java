package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.sanity.SanityConstants;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class DreamInstance {
    private final UUID id;
    private DreamArea area;

    private final Map<UUID, PlayerSnapshot> players = new HashMap<>();

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
        return Set.copyOf(players.keySet());
    }

    public void addPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();

        if (players.containsKey(playerId)) {
            return;
        }

        PlayerSnapshot snapshot = PlayerSnapshot.capture(player);
        players.put(playerId, snapshot);

        applyDreamState(player, getInitialPlayerState(player));
    }

    public void playerLeft(ServerPlayer player) {
        UUID playerId = player.getUUID();

        PlayerSnapshot snapshot = players.remove(playerId);

        if (snapshot != null) {
            snapshot.restore(player);
        }
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    protected DreamPlayerState getInitialPlayerState(ServerPlayer player) {
        return DreamPlayerState.defaults();
    }

    protected void applyDreamState(ServerPlayer player, DreamPlayerState state) {
        if (state.clearInventory()) {
            player.getInventory().clearContent();
        }

        if (state.clearEffects()) {
            for (MobEffectInstance effect : player.getActiveEffects().toArray(new MobEffectInstance[0])) {
                player.removeEffect(effect.getEffect());
            }
        }

        if (state.resetHealth()) {
            if (state.health() < 0.0F) {
                player.setHealth(player.getMaxHealth());
            } else {
                player.setHealth(Math.min(state.health(), player.getMaxHealth()));
            }
        }

        if (state.resetFood()) {
            FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(state.foodLevel());
            foodData.setSaturation(state.saturationLevel());
            foodData.setExhaustion(state.exhaustionLevel());
        }

        if (state.resetExperience()) {
            player.experienceLevel = state.experienceLevel();
            player.totalExperience = state.totalExperience();
            player.experienceProgress = state.experienceProgress();
        }

        if (state.resetSanity()) {
            SanitySystem.set(player, state.sanity());
        }

        if (state.resetSelectedSlot()) {
            player.getInventory().selected = state.selectedSlot();
        }

        if (state.resetAbilities()) {
            player.getAbilities().invulnerable = state.invulnerable();
            player.getAbilities().flying = state.flying();
            player.getAbilities().mayfly = state.mayfly();
            player.getAbilities().instabuild = state.instabuild();
            player.getAbilities().mayBuild = state.mayBuild();
        }

        if (state.resetFire()) {
            player.clearFire();
        }

        if (state.resetFrozenTicks()) {
            player.setTicksFrozen(0);
        }

        if (state.resetFallDistance()) {
            player.resetFallDistance();
        }

        if (state.resetMovement()) {
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
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

    protected static class PlayerSnapshot {
        private final float health;
        private final float sanity;
        private final List<ItemStack> inventory;
        private final int selectedSlot;
        private final int experienceLevel;
        private final int totalExperience;
        private final float experienceProgress;
        private final int foodLevel;
        private final float saturationLevel;
        private final float exhaustionLevel;
        private final List<MobEffectInstance> effects;
        private final Abilities abilities;

        private PlayerSnapshot(float health, float sanity, List<ItemStack> inventory, int selectedSlot, int experienceLevel,
                               int totalExperience, float experienceProgress, int foodLevel, float saturationLevel,
                               float exhaustionLevel, List<MobEffectInstance> effects, Abilities abilities) {
            this.health = health;
            this.sanity = sanity;
            this.inventory = inventory;
            this.selectedSlot = selectedSlot;
            this.experienceLevel = experienceLevel;
            this.totalExperience = totalExperience;
            this.experienceProgress = experienceProgress;
            this.foodLevel = foodLevel;
            this.saturationLevel = saturationLevel;
            this.exhaustionLevel = exhaustionLevel;
            this.effects = effects;
            this.abilities = abilities;
        }

        public static PlayerSnapshot capture(ServerPlayer player) {
            List<ItemStack> inventory = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                inventory.add(player.getInventory().getItem(i).copy());
            }

            List<MobEffectInstance> effects = new ArrayList<>();
            for (MobEffectInstance effect : player.getActiveEffects()) {
                effects.add(new MobEffectInstance(effect));
            }

            Abilities abilities = new Abilities();
            abilities.invulnerable = player.getAbilities().invulnerable;
            abilities.flying = player.getAbilities().flying;
            abilities.mayfly = player.getAbilities().mayfly;
            abilities.instabuild = player.getAbilities().instabuild;
            abilities.mayBuild = player.getAbilities().mayBuild;
            abilities.setFlyingSpeed(player.getAbilities().getFlyingSpeed());
            abilities.setWalkingSpeed(player.getAbilities().getWalkingSpeed());

            FoodData foodData = player.getFoodData();

            return new PlayerSnapshot(
                    player.getHealth(),
                    SanitySystem.get(player),
                    inventory,
                    player.getInventory().selected,
                    player.experienceLevel,
                    player.totalExperience,
                    player.experienceProgress,
                    foodData.getFoodLevel(),
                    foodData.getSaturationLevel(),
                    foodData.getExhaustionLevel(),
                    effects,
                    abilities
            );
        }

        public void restore(ServerPlayer player) {
            player.setHealth(Math.min(health, player.getMaxHealth()));

            for (int i = 0; i < inventory.size(); i++) {
                player.getInventory().setItem(i, inventory.get(i).copy());
            }

            player.getInventory().selected = selectedSlot;

            player.experienceLevel = experienceLevel;
            player.totalExperience = totalExperience;
            player.experienceProgress = experienceProgress;

            FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(foodLevel);
            foodData.setSaturation(saturationLevel);
            foodData.setExhaustion(exhaustionLevel);

            SanitySystem.set(player, sanity);

            for (MobEffectInstance effect : player.getActiveEffects().toArray(new MobEffectInstance[0])) {
                player.removeEffect(effect.getEffect());
            }

            for (MobEffectInstance effect : effects) {
                player.addEffect(new MobEffectInstance(effect));
            }

            player.getAbilities().invulnerable = abilities.invulnerable;
            player.getAbilities().flying = abilities.flying;
            player.getAbilities().mayfly = abilities.mayfly;
            player.getAbilities().instabuild = abilities.instabuild;
            player.getAbilities().mayBuild = abilities.mayBuild;
            player.getAbilities().setFlyingSpeed(abilities.getFlyingSpeed());
            player.getAbilities().setWalkingSpeed(abilities.getWalkingSpeed());

            player.clearFire();
            player.setTicksFrozen(0);
        }
    }
}