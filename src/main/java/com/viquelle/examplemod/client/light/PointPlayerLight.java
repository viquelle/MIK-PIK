package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class PointPlayerLight extends AbstractPlayerLight<PointLightData>{

    @Override
    public PointPlayerLight create() {
        light = new PointLightData();
        light.setBrightness(1.0f)
                .setRadius(6f)
                .setColor(color);

        handle = VeilRenderSystem.renderer()
                .getLightRenderer()
                .addLight(light);

        active = true;
        return this;
    }

    @Override
    public void remove() {

    }

//    @Override
//    public void destroy() {
//        if (handle != null) {
//            handle.free();
//            handle = null;
//        }
//        active = false;
//    }

    @Override
    public void update(LocalPlayer player, float partialTick) {
        if (!active) return;

        Vec3 pos = player.getEyePosition(partialTick);
        light.setPosition((float)pos.x,(float)pos.y,(float)pos.z);
    }

}
