package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.entity.firefly.FireflyEntity;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class FireflyLightSource implements LightSource {
    private final Map<Integer, FireflyLightState> fireflies = new HashMap<>();

    private static final float RADIUS = 3.0f;
    private static final float TARGET_BRIGHTNESS = 1.0f;
    private static final float INSCATTER_STRENGTH = 8f;
    private static final int COLOR = 0xC3FFAD;
    private static final boolean OCCLUSION = true;
    private static final float FADE_SPEED = 1.0f / 4.0f;

    private float currentDeltaTime;

    private static class FireflyLightState {
        final FireflyEntity firefly;
        PointLightHandle light;

        float currentBrightness = 0f;
        boolean shouldRemove = false;

        FireflyLightState(FireflyEntity firefly) {
            this.firefly = firefly;

            if (!firefly.isDormant()) {
                currentBrightness = TARGET_BRIGHTNESS;
                createLight();
            }
        }

        void createLight() {
            if (light != null) return;

            light = new PointLightHandle(
                    RADIUS,
                    currentBrightness,
                    COLOR,
                    OCCLUSION,
                    true,
                    INSCATTER_STRENGTH
            );

            light.register();
            light.setPosition(firefly.position().add(0,firefly.getBoundingBox().getYsize()/2,0));
        }

        void update(Vec3 newPos, float deltaTime) {
            if (light == null) return;
            light.setPosition(newPos);

            if (shouldRemove) {
                currentBrightness = Math.clamp(currentBrightness - FADE_SPEED * deltaTime, 0f, TARGET_BRIGHTNESS);

                if (currentBrightness == 0f) {
                    light.unregister();
                    light = null;

                    shouldRemove = false;
                    return;
                }
            } else {
                currentBrightness = Math.clamp(currentBrightness + FADE_SPEED * deltaTime, 0f, TARGET_BRIGHTNESS);
            }

            light.setBrightness(currentBrightness);
        }

        void wakeUp() {
            if (light != null) return;

            shouldRemove = false;
            currentBrightness = 0f;
            createLight();
        }

        void kill() {
            if (light != null) {
                light.unregister();
                light = null;
            }

        }
    }

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;

        currentDeltaTime = (level.getGameTime() + partialTick - ClientLightManager.getLastRenderTick()) / 20f;

        Set<Integer> aliveIds = new HashSet<>();
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            List<FireflyEntity> found = level.getEntitiesOfClass(FireflyEntity.class, mc.player.getBoundingBox().inflate(64));

            for (FireflyEntity firefly : found) {
                if (firefly.isRemoved()) continue;

                int id = firefly.getId();
                aliveIds.add(id);

                FireflyLightState state = fireflies.get(id);

                if (state == null) {
                    state = new FireflyLightState(firefly);
                    fireflies.put(id, state);

                    //MikpikMod.LOGGER.info("FireflyLightSource: found new firefly #{}", id);
                }

                if (firefly.isDormant()) {
                    if (state.light != null) state.shouldRemove = true;
                } else {
                    if (state.light == null) {
                        state.wakeUp();
                        //MikpikMod.LOGGER.info("FireflyLightSource: firefly #{} woke up", id);
                    }
                }

                state.update(
                        firefly.position().add(0, firefly.getBoundingBox().getYsize() / 2f, 0),
                        currentDeltaTime
                );
            }
        }

        // Обрабатываем светлячков, которые больше не загружены
        Iterator<Map.Entry<Integer, FireflyLightState>> it =
                fireflies.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, FireflyLightState> entry = it.next();
            if (aliveIds.contains(entry.getKey())) continue;

            FireflyLightState state = entry.getValue();
            state.shouldRemove = true;

            state.update(
                    state.firefly.position().add(0, state.firefly.getBoundingBox().getYsize() / 2f, 0),
                    currentDeltaTime
            );

            if (state.light == null) {
                it.remove();

                //MikpikMod.LOGGER.info("FireflyLightSource: firefly #{} disappeared, removing", entry.getKey());
            }
        }
    }

    @Override
    public void destroy() {
        for (FireflyLightState state : fireflies.values()) {
            state.kill();
        }

        fireflies.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        List<PointLightHandle> buffer =
                new ArrayList<>(fireflies.size());

        for (FireflyLightState state : fireflies.values()) {
            if (state.light != null) {
                buffer.add(state.light);
            }
        }

        return buffer;
    }
}