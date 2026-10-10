package com.viquelle.mikpik.light;

import com.viquelle.mikpik.sanity.SanityConstants;
import com.viquelle.mikpik.util.WorldLightMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerLightManager {
    private static final Map<UUID, Float> playerDynamicBright = new HashMap<>();
    private static final Map<UUID, Long> lastUpdate = new HashMap<>();

    public static void setPlayerDynamicBright(UUID playerId, float bright) {
        playerDynamicBright.put(playerId, bright);
        lastUpdate.put(playerId, System.currentTimeMillis());
    }

    public static float getPlayerDynamicBright(UUID playerId) {
        Long time = lastUpdate.get(playerId);
        if (time == null || System.currentTimeMillis() - time > 3000) {
            return 0f;
        }
        return playerDynamicBright.getOrDefault(playerId, 0f) * SanityConstants.VEIL_NORMALIZATION;
    }

    public static boolean isPlayerInDark(Player player) {
        Level level = player.level();
        Vec3 pos = player.getPosition(1f);
        float veilLight = getPlayerDynamicBright(player.getUUID());
        float blockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos));
        float skyLight = WorldLightMath.posSkyLight(pos, level, 1.0f);
        float localBrightness = Math.max(Math.max(blockLight, skyLight), veilLight);
        return localBrightness <= SanityConstants.BRIGHTNESS_THRESHOLD;
    }
}
