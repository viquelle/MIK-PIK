package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.sleep.DreamDimension;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelAccessor.class)
public interface LevelAccessorMixin {

    /**
     * @author mikpik
     * @reason Give dream dimensions independent logical day time.
     */
    @Overwrite
    default long dayTime() {
        LevelAccessor level = (LevelAccessor) this;

        if (level instanceof net.minecraft.world.level.Level actualLevel) {
            if (actualLevel.dimension() == DreamDimension.DAY) {
                return 6000L;
            }

            if (actualLevel.dimension() == DreamDimension.NIGHT_FULL_MOON) {
                return 18000L;
            }

            if (actualLevel.dimension() == DreamDimension.NIGHT_NEW_MOON) {
                return 114000L;
            }
        }

        return level.getLevelData().getDayTime();
    }
}