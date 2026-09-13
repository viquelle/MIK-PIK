package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.entity.watcher.WatcherEntity;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class WatcherEntityLightSource implements LightSource {
    private static final float RADIUS = 8.0f;
    private static final float TARGET_BRIGHTNESS = -1.0f;
    private static final int COLOR = 0xFFFFFF;
    private static final boolean OCCLUSION = true;
    private static final float FADE_OUT_SPEED = 1.0f;

    private final Map<Integer, WatcherLightState> watchers = new HashMap<>();
    private float currentDeltaTime;

    private static class WatcherLightState {
        final PointLightHandle light;
        float currentBrightness;
        boolean shouldRemove = false;
        boolean isDead = false;

        WatcherLightState(Vec3 initialPos) {
            this.currentBrightness = TARGET_BRIGHTNESS;
            this.light = new PointLightHandle(RADIUS, currentBrightness, COLOR, OCCLUSION, false, 0f);
            this.light.register();
            this.light.setPosition(initialPos);
        }

        void update(Vec3 newPos, float deltaTime) {
            light.setPosition(newPos);

            if (shouldRemove) {
                currentBrightness = Math.clamp(
                        currentBrightness + FADE_OUT_SPEED * deltaTime,
                        TARGET_BRIGHTNESS,
                        0f
                );
                if (currentBrightness >= -0.001f) {
                    isDead = true;
                }
            }
            light.setBrightness(currentBrightness);
        }

        void kill() {
            light.unregister();
        }
    }

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;

        currentDeltaTime = (level.getGameTime() + partialTick - ClientLightManager.getLastRenderTick()) / 20f;

        Set<Integer> aliveIds = new HashSet<>();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            List<WatcherEntity> found = level.getEntitiesOfClass(
                    WatcherEntity.class,
                    mc.player.getBoundingBox().inflate(64)
            );

            for (WatcherEntity watcher : found) {
                if (watcher.isRemoved()) continue;

                int id = watcher.getId();
                aliveIds.add(id);

                WatcherLightState state = watchers.get(id);
                if (state == null) {
                    state = new WatcherLightState(watcher.position());
                    watchers.put(id, state);
                    //MikpikMod.LOGGER.info("WatcherLightSource: found new watcher #{}", id);
                } else {
                    state.update(watcher.position(), currentDeltaTime);
                }
            }
        }

        for (Map.Entry<Integer, WatcherLightState> entry : watchers.entrySet()) {
            int id = entry.getKey();
            WatcherLightState state = entry.getValue();

            if (!aliveIds.contains(id) && !state.shouldRemove) {
                state.shouldRemove = true;
                //MikpikMod.LOGGER.info("WatcherLightSource: watcher #{} disappeared, starting fade-out", id);
            }
        }

        for (WatcherLightState state : watchers.values()) {
            state.update(state.light.getPosition(), currentDeltaTime);
        }

        Iterator<Map.Entry<Integer, WatcherLightState>> it = watchers.entrySet().iterator();
        while (it.hasNext()) {
            WatcherLightState state = it.next().getValue();
            if (state.isDead) {
                state.kill();
                it.remove();
                //MikpikMod.LOGGER.info("WatcherLightSource: removed dead watcher");
            }
        }
    }

    @Override
    public void destroy() {
        for (WatcherLightState state : watchers.values()) {
            state.kill();
        }
        watchers.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        List<PointLightHandle> buffer = new ArrayList<>(watchers.size());
        for (WatcherLightState s : watchers.values()) {
            buffer.add(s.light);
        }
        return buffer;
    }
}