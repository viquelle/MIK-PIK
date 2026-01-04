package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class AreaLight extends AbstractLight<AreaLightData>{
    protected Vec3 pos; // EYEPOS
    protected Quaternionf orientation; // PITCH AND YAW EYES
    public Vec3 lastPos = null;
    public Quaternionf lastOrientation = null;

    public AreaLight(Builder builder) {
        super(builder);
        this.lastPos = builder.pos;
        this.lastOrientation = builder.orientation;
    }

    public void syncPos() {
        pos = lastPos;
        handle.getLightData().getPosition().set(
                pos.x,
                pos.y,
                pos.z
        );
    }

    public void syncOrientation() {
        orientation = lastOrientation;
        handle.getLightData().getOrientation().set(orientation);
    }

    public void syncWithObj(Player p, float pt) {
        Quaternionf orientation = new Quaternionf().rotateXYZ(
                (float) -Math.toRadians(p.getXRot()),
                (float) Math.toRadians(p.getYRot()),
                0.0f
        );
        Vec3 eyePos = p.getEyePosition(pt);
        this.lastOrientation = orientation;
        this.lastPos = eyePos;
    }

    //    public void update(LocalPlayer player, float partialTick) {
//        ExampleMod.LOGGER.info("[TICK] TRYING TO TICK");
//        if (!active) return;
//        ExampleMod.LOGGER.info("[TICK] ACTIVE PASSED!");
//
//        float pitch = (float) -Math.toRadians(player.getXRot());
//        float yaw = (float) Math.toRadians(player.getYRot());
//        Quaternionf orientation = new Quaternionf().rotateXYZ(pitch,yaw,0.0f);
//        light.getOrientation().set(orientation);
//
//        Vec3 eyePos = player.getEyePosition(partialTick);
//        light.getPosition().set(
//                (float) eyePos.x,
//                (float) eyePos.y,
//                (float) eyePos.z
//        );
//        ExampleMod.LOGGER.info("[TICK] rot: {} || eyePos: {}", orientation,eyePos);
//    }
    public static class Builder extends AbstractLight.Builder<AreaLight.Builder> {
        private Vec3 pos;
        private Quaternionf orientation;

        public Builder setPosition(Vec3 pos) {
            this.pos = pos;
            return this;
        }

        public Builder setPosition(double x, double y, double z) {
            this.pos = new Vec3(x,y,z);
            return this;
        }

        public Builder setOrientation(Quaternionf orientation) {
            this.orientation = orientation;
            return this;
        }

        @Override
        public AreaLight build() {
            return new AreaLight(this);
        }


    }

    @Override
    public void register() {
        AreaLightData light = new AreaLightData();
        light.setBrightness(lastBrightness)
                .setColor(lastColor)
                .setDistance(32f)
                .setAngle(0.6f)
                .setSize(0.1f,0.1f);

        handle = VeilRenderSystem.renderer()
                .getLightRenderer()
                .addLight(light);

        registered = true;
    }

    @Override
    public void tick(float partialTick) {
        if (isDirty && registered) {
            super.tick(partialTick, handle);
            if (orientation != lastOrientation) syncOrientation();
            if (pos != lastPos) syncPos();

            //isDirty = false;
        }
    }


}
