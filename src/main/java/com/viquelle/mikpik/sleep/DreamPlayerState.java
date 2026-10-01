package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.sanity.SanityConstants;

/**
 * Defines the player state used when entering a particular dream.
 *
 * Negative health means "use the player's maximum health".
 */
public record DreamPlayerState(
        float health,
        int foodLevel,
        float saturationLevel,
        float exhaustionLevel,
        float sanity,
        int experienceLevel,
        int totalExperience,
        float experienceProgress,
        int selectedSlot,
        boolean clearInventory,
        boolean clearEffects,
        boolean resetHealth,
        boolean resetFood,
        boolean resetExperience,
        boolean resetSanity,
        boolean resetSelectedSlot,
        boolean resetAbilities,
        boolean invulnerable,
        boolean flying,
        boolean mayfly,
        boolean instabuild,
        boolean mayBuild,
        boolean resetFire,
        boolean resetFrozenTicks,
        boolean resetFallDistance,
        boolean resetMovement
) {

    public static DreamPlayerState defaults() {
        return new DreamPlayerState(
                -1.0F,
                20,
                20.0F,
                0.0F,
                SanityConstants.MAX_SANITY,
                0,
                0,
                0.0F,
                0,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                true
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float health = -1.0F;
        private int foodLevel = 20;
        private float saturationLevel = 20.0F;
        private float exhaustionLevel = 0.0F;
        private float sanity = SanityConstants.MAX_SANITY;
        private int experienceLevel = 0;
        private int totalExperience = 0;
        private float experienceProgress = 0.0F;
        private int selectedSlot = 0;
        private boolean clearInventory = true;
        private boolean clearEffects = true;
        private boolean resetHealth = true;
        private boolean resetFood = true;
        private boolean resetExperience = true;
        private boolean resetSanity = true;
        private boolean resetSelectedSlot = true;
        private boolean resetAbilities = true;
        private boolean invulnerable = false;
        private boolean flying = false;
        private boolean mayfly = false;
        private boolean instabuild = false;
        private boolean mayBuild = true;
        private boolean resetFire = true;
        private boolean resetFrozenTicks = true;
        private boolean resetFallDistance = true;
        private boolean resetMovement = true;

        public Builder health(float value) { this.health = value; return this; }
        public Builder foodLevel(int value) { this.foodLevel = value; return this; }
        public Builder saturationLevel(float value) { this.saturationLevel = value; return this; }
        public Builder exhaustionLevel(float value) { this.exhaustionLevel = value; return this; }
        public Builder sanity(float value) { this.sanity = value; return this; }
        public Builder experienceLevel(int value) { this.experienceLevel = value; return this; }
        public Builder totalExperience(int value) { this.totalExperience = value; return this; }
        public Builder experienceProgress(float value) { this.experienceProgress = value; return this; }
        public Builder selectedSlot(int value) { this.selectedSlot = value; return this; }
        public Builder clearInventory(boolean value) { this.clearInventory = value; return this; }
        public Builder clearEffects(boolean value) { this.clearEffects = value; return this; }
        public Builder resetHealth(boolean value) { this.resetHealth = value; return this; }
        public Builder resetFood(boolean value) { this.resetFood = value; return this; }
        public Builder resetExperience(boolean value) { this.resetExperience = value; return this; }
        public Builder resetSanity(boolean value) { this.resetSanity = value; return this; }
        public Builder resetSelectedSlot(boolean value) { this.resetSelectedSlot = value; return this; }
        public Builder resetAbilities(boolean value) { this.resetAbilities = value; return this; }
        public Builder invulnerable(boolean value) { this.invulnerable = value; return this; }
        public Builder flying(boolean value) { this.flying = value; return this; }
        public Builder mayfly(boolean value) { this.mayfly = value; return this; }
        public Builder instabuild(boolean value) { this.instabuild = value; return this; }
        public Builder mayBuild(boolean value) { this.mayBuild = value; return this; }
        public Builder resetFire(boolean value) { this.resetFire = value; return this; }
        public Builder resetFrozenTicks(boolean value) { this.resetFrozenTicks = value; return this; }
        public Builder resetFallDistance(boolean value) { this.resetFallDistance = value; return this; }
        public Builder resetMovement(boolean value) { this.resetMovement = value; return this; }

        public DreamPlayerState build() {
            return new DreamPlayerState(
                    health, foodLevel, saturationLevel, exhaustionLevel, sanity,
                    experienceLevel, totalExperience, experienceProgress, selectedSlot,
                    clearInventory, clearEffects, resetHealth, resetFood, resetExperience,
                    resetSanity, resetSelectedSlot, resetAbilities, invulnerable, flying,
                    mayfly, instabuild, mayBuild, resetFire, resetFrozenTicks,
                    resetFallDistance, resetMovement
            );
        }
    }
}
