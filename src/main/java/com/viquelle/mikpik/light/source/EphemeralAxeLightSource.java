package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class EphemeralAxeLightSource implements LightSource {

    private static final float RADIUS = 5.0f;
    private static final float TARGET_BRIGHTNESS = 1.0f;

    private static final int COLOR = 0xFF82D8;

    private static final boolean OCCLUSION = true;

    private static final float INSCATTER_MIN_DISTANCE = 1.5f;
    private static final float INSCATTER_MAX_DISTANCE = 5.0f;
    private static final float MAX_INSCATTERING = 25.0f;

    private static final float LIT_SPEED = 0.75f;
    private static final float USUAL_EXTING_SPEED = -2.0f;
    private static final float SUPER_EXTING_SPEED = -5.0f;

    private float currentPartialTick;
    private float currentDeltaTime;

    private final Map<String, AxeLightState> axes = new HashMap<>();

    private class AxeLightState {

        private final Entity owner;
        private final PointLightHandle light;

        private float currentBrightness = 0.0f;

        private boolean shouldRemove = false;
        private boolean isDead = false;

        AxeLightState(Entity owner) {
            this.owner = owner;

            this.light = new PointLightHandle(
                    RADIUS,
                    currentBrightness,
                    COLOR,
                    OCCLUSION,
                    true,
                    0.0f
            );

            this.light.register();
        }

        void update() {
            if (owner.isRemoved()) {
                shouldRemove = true;
            }

            updatePosition();
            updateBrightness();
            updateInscattering();

            if (shouldRemove && currentBrightness < 0.001f) {
                isDead = true;
            }
        }

        private void updatePosition() {
            Vec3 position;

            if (owner instanceof ItemEntity itemEntity) {
                position = itemEntity.getPosition(currentPartialTick).add(0.0, itemEntity.getBbHeight() * 0.5,0.0);
            } else if (owner instanceof Player player) {
                position = player.getPosition(currentPartialTick).add(0.0, player.getBbHeight() * 0.65, 0.0);
            } else {
                position = owner.getPosition(currentPartialTick).add(0.0, owner.getBbHeight() * 0.5, 0.0);
            }

            light.setPosition(position);
        }

        private void updateBrightness() {
            float currentSpeed;

            if (owner.isRemoved()) {
                currentSpeed = SUPER_EXTING_SPEED;
            } else if (shouldRemove) {
                currentSpeed = USUAL_EXTING_SPEED;
            } else {
                currentSpeed = LIT_SPEED;
            }

            currentBrightness = Mth.clamp(
                    currentBrightness + currentSpeed * currentDeltaTime,
                    0.0f,
                    TARGET_BRIGHTNESS
            );

            light.setBrightness(currentBrightness);
        }

        private void updateInscattering() {
            Minecraft mc = Minecraft.getInstance();

            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3 lightPos = light.getPosition();
            double distance = cameraPos.distanceTo(lightPos);

            float t = Mth.clamp(
                    (float) (
                            (distance - INSCATTER_MIN_DISTANCE)
                                    / (INSCATTER_MAX_DISTANCE - INSCATTER_MIN_DISTANCE)
                    ),
                    0.0f,
                    1.0f
            );

            t = t * t * (3.0f - 2.0f * t);

            float inscattering = MAX_INSCATTERING * t;

            inscattering *= currentBrightness;

            light.setInscattering(inscattering);
        }

        void kill() {
            light.unregister();
        }
    }

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;

        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;

        if (localPlayer == null) return;

        currentPartialTick = partialTick;
        currentDeltaTime = (level.getGameTime() + partialTick - ClientLightManager.getLastRenderTick())/ 20.0f;


        for (AxeLightState state : axes.values()) {
            state.shouldRemove = true;
        }

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;

            ItemStack mainHand = player.getMainHandItem();

            if (isEphemeralAxe(mainHand)) {
                String key = "player_" + player.getUUID() + "_main";
                activateOrFlag(player, key);
            }

            ItemStack offHand = player.getOffhandItem();

            if (isEphemeralAxe(offHand)) {
                String key = "player_" + player.getUUID() + "_off";
                activateOrFlag(player, key);
            }
        }

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, localPlayer.getBoundingBox().inflate(48.0))) {
            if (itemEntity.isRemoved()) continue;

            ItemStack stack = itemEntity.getItem();
            if (!isEphemeralAxe(stack)) continue;

            String key = "item_" + itemEntity.getId();
            activateOrFlag(itemEntity, key);
        }

        Iterator<Map.Entry<String, AxeLightState>> iterator = axes.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, AxeLightState> entry = iterator.next();
            AxeLightState state = entry.getValue();

            state.update();

            if (state.isDead) {
                state.kill();
                iterator.remove();
            }
        }
    }

    private boolean isEphemeralAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.EPHEMERAL_AXE.get());
    }

    private void activateOrFlag(Entity entity, String key) {
        AxeLightState state = axes.get(key);

        if (state == null) {
            state = new AxeLightState(entity);
            axes.put(key, state);
        }

        state.shouldRemove = false;
    }

    @Override
    public void destroy() {
        for (AxeLightState state : axes.values()) {
            state.kill();
        }

        axes.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        List<PointLightHandle> lights = new ArrayList<>(axes.size());

        for (AxeLightState state : axes.values()) {
            lights.add(state.light);
        }

        return lights;
    }
}