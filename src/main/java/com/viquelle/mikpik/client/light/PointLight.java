package com.viquelle.mikpik.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PointLight extends AbstractLight<PointLightData> {
    private final float radius;
    private final Vector3f posCache = new Vector3f();

    protected PointLight(Builder builder) {
        super(builder);
        this.radius = builder.radius;
    }

    @Override
    public void tick(float dt, float partialTick) {
        Player p = getPlayer();
        if (p == null || handle == null) return;

        super.tick(dt, partialTick);

        syncPosition(p, partialTick);
    }

    private void syncPosition(Player p, float pt) {
        Vec3 eyePos = p.getEyePosition(pt);
        this.posCache.set((float)eyePos.x, (float)eyePos.y, (float)eyePos.z);

        PointLightData data = handle.getLightData();
        data.setPosition(posCache.x, posCache.y, posCache.z);
    }

    @Override
    public void register() {
        PointLightData lightData = new PointLightData();
        lightData.setBrightness(currentBrightness)
                .setRadius(radius)
                .setColor(color);

        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            handle = VeilRenderSystem.renderer()
                    .getLightRenderer()
                    .addLight(lightData);
        });
    }

    public float getRadius() { return radius; }

    public static class Builder extends AbstractLight.Builder<PointLight.Builder> {
        protected float radius = 6f;

        public Builder(Player player) {
            super(player);
        }

        public Builder setRadius(float radius) {
            this.radius = radius;
            return this;
        }

        @Override
        public PointLight build() {
            return new PointLight(this);
        }
    }
}