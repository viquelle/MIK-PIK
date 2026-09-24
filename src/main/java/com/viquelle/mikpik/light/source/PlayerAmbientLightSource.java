package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public class PlayerAmbientLightSource implements LightSource{
    private static final int COLOR = 0x0B6A99;
    private static final float BASE_BRIGHTNESS = 1f;
    private static final float MAX_RADIUS = 6f;
    private float CURRENT_RADIUS = 0f;
    private float CURRENT_BRIGHTNESS = 0f;;

    private static final float PROGRESS_TIME = 5f; // 5 seconds
    private boolean registered = false;
    private final PointLightHandle light = new PointLightHandle(0, BASE_BRIGHTNESS, COLOR, true, false, 0);

    @Override
    public void tick(Level level, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null || !level.isClientSide) return;

        if (!registered) {
            light.register();
            registered = true;
        }

        float now = level.getGameTime() + partialTick;
        float deltaSeconds = Math.max((now - ClientLightManager.getLastRenderTick()) / 20f, 0f);

        boolean isDark = ClientLightManager.isDarkOnPos(
                player.getEyePosition(partialTick),
                level,
                partialTick);

        CURRENT_RADIUS = MAX_RADIUS;

        float targetBrightness = isDark ? BASE_BRIGHTNESS : 0.0f;

        if (CURRENT_BRIGHTNESS < targetBrightness) {
            CURRENT_BRIGHTNESS = Math.min(
                    CURRENT_BRIGHTNESS + (BASE_BRIGHTNESS / PROGRESS_TIME) * deltaSeconds,
                    targetBrightness
            );
        } else if (CURRENT_BRIGHTNESS > targetBrightness) {
            CURRENT_BRIGHTNESS = Math.max(
                    CURRENT_BRIGHTNESS - (BASE_BRIGHTNESS / PROGRESS_TIME) * deltaSeconds,
                    0f
            );
        }
        light.setBrightness(CURRENT_BRIGHTNESS);
        light.setRadius(CURRENT_RADIUS);
        light.setPosition(player.getEyePosition(partialTick));
    }

    @Override
    public void destroy() {
        if (registered) {
            light.unregister();
            registered = false;
        }
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        return List.of(light);
    }

    @Override
    public UpdatePhase getUpdatePhase() {
        return UpdatePhase.AFTER_LIGHTS;
    }
}