package com.viquelle.examplemod.client.light;

import com.viquelle.examplemod.ExampleMod;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class AreaPlayerLight extends AbstractPlayerLight<AreaLightData>{
    public AreaPlayerLight create() {
        light = new AreaLightData();
        light.setBrightness(0.7f)
                .setDistance(32f)
                .setAngle(0.6f)
                .setSize(0.1f,0.1f)
                .setColor(color);

        handle = VeilRenderSystem.renderer()
                .getLightRenderer()
                .addLight(light);

        active = true;
        return this;
    }

    @Override
    public void update(LocalPlayer player, float partialTick) {
        ExampleMod.LOGGER.info("[TICK] TRYING TO TICK");
        if (!active) return;
        ExampleMod.LOGGER.info("[TICK] ACTIVE PASSED!");

        float pitch = (float) -Math.toRadians(player.getXRot());
        float yaw = (float) Math.toRadians(player.getYRot());
        Quaternionf orientation = new Quaternionf().rotateXYZ(pitch,yaw,0.0f);
        light.getOrientation().set(orientation);

        Vec3 eyePos = player.getEyePosition(partialTick);
        light.getPosition().set(
                (float) eyePos.x,
                (float) eyePos.y,
                (float) eyePos.z
        );
        ExampleMod.LOGGER.info("[TICK] rot: {} || eyePos: {}", orientation,eyePos);
    }

    @Override
    public void remove() {

    }
}
