package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.light.ServerLightManager;
import com.viquelle.mikpik.sanity.SanityConstants;
import com.viquelle.mikpik.util.WorldLightMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class LightFactor implements SanityFactor {
    @Override
    public float getDelta(Player player) {
        Level level = player.level();
        Vec3 pos = player.getEyePosition(1f);

        float skyLight = WorldLightMath.posSkyLight(pos, level, 1f);

        // Дневной свет неба регенит рассудок.
        if (level.isDay() && skyLight > 8f) {
            return SanityConstants.SKY_DAY_REGEN_PER_TICK;
        }

        float blocklight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos));
        if (ServerLightManager.isPlayerInDark(player)) {
            return SanityConstants.DARK_DRAIN_PER_TICK;
        } else if (skyLight <= SanityConstants.BRIGHTNESS_THRESHOLD && blocklight <= SanityConstants.BRIGHTNESS_THRESHOLD) {
            return SanityConstants.DARK_DRAIN_PER_TICK / 10.f;
        }

        return 0.0f;
    }
}