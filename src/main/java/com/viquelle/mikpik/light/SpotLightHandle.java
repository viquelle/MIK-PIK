package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.SpotLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

public class SpotLightHandle extends LightHandle<SpotLightData> {
    private final Quaternionf direction = new Quaternionf();
    private float angle;
    private float range;
    private float size;
    private boolean occlusion;
    private float inscatter;

    public SpotLightHandle(float size, float angle, float range, float brightness, int color, boolean occlusion, boolean countsAsLight, float inscatter) {
        super(new SpotLightData(), brightness, color, countsAsLight);
        this.angle = angle;
        this.range = range;
        this.brightness = brightness;
        this.color = color;
        this.occlusion = occlusion;
        this.affectDarkness = countsAsLight;
        this.inscatter = inscatter;
        data.setSize(size)
                .setAngle(angle)
                .setDistance(range)
                .setBrightness(brightness)
                .setColor(color)
                .setInscatteringStrength(inscatter)
                .setOcclusionEnabled(occlusion);
    }

    /**
     * Устанавливает ориентацию света по направлению взгляда игрока.
     */
    public void setOrientationFromPlayer(Player player, float partialTick) {
        Vec3 look = player.getViewVector(partialTick);

        Vector3f target = new Vector3f(
                (float) look.x,
                (float) look.y,
                (float) -look.z
        ).normalize();

        direction.identity().rotationTo(
                new Vector3f(0, 0, -1),
                target
        );

        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.getOrientationMutable().set(direction));
        }
    }

    public void setAngle(float angle) {
        this.angle = angle;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setAngle(angle));
        }
    }
    public float getAngle() {
        return angle;
    }

    public void setRange(float range) {
        this.range = range;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setDistance(range));
        }
    }
    public float getRange() {
        return range;
    }

    public Quaternionf getDirection() {
        return direction;
    }

    @Override
    protected void updatePositionInData(Vec3 pos) {
        data.getPositionMutable().set(pos.x, pos.y, pos.z);
    }

    public Vec3 getForward() {
        Vector3f v = new Vector3f(0, 0, -1);
        direction.transform(v);
        return new Vec3(v.x, v.y, -v.z).normalize();
    }

    public void setInscattering(float value) {
        this.inscatter = value;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setInscatteringStrength(inscatter));
        }
    }
}