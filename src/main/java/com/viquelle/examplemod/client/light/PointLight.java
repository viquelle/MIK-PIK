package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

public class PointLight extends AbstractLight<PointLightData>{
    private Vec3 pos;
    public Vec3 lastPos;

    protected PointLight(Builder builder) {
        super(builder);
        this.lastPos = builder.pos;
    }

    protected void syncPos(LightRenderHandle<PointLightData> handle){
        this.pos = lastPos;
        handle.getLightData().setPosition((Vector3dc) pos);
    }

    public static class Builder extends AbstractLight.Builder<PointLight.Builder> {
        private Vec3 pos;

        public PointLight.Builder setPosition(Vec3 pos) {
            this.pos = pos;
            return this;
        }

        public PointLight.Builder setPosition(double x, double y, double z) {
            this.pos = new Vec3(x,y,z);
            return this;
        }

        @Override
        public PointLight build() {
            return new PointLight(this);
        }
    }
//    @Override
//    public IAbstractLight create() {
//        light = new PointLightData();
//        light.setBrightness(1.0f)
//                .setRadius(6f)
//                .setColor(0xFFFFFF);
//
//        handle = VeilRenderSystem.renderer()
//                .getLightRenderer()
//                .addLight(light);
//
//        active = true;
//    }

    @Override
    public void tick(float partialTick) {
        if (isDirty && registered) {
            super.tick(partialTick, handle);

            if (pos != lastPos) {
                syncPos(handle);
            }
            //isDirty = false;
        }
    }

    //    @Override
//    public void tick(float partialTick) {
//        super.tick(partialTick);
//        AreaLightData light = handle.getLightData();
//        light.getOrientation().set(orientation);
//        light.getPosition().set((Vector3dc) pos);
//        light.setBrightness(lastBrightness);
//        light.setColor(lastColor);
//    }

    @Override
    public void register() {
        PointLightData light = new PointLightData();
        light.setBrightness(lastBrightness)
                .setColor(lastColor)
                .setRadius(6f)
                .setPosition((Vector3dc) lastPos);

        handle = VeilRenderSystem.renderer()
                .getLightRenderer()
                .addLight(light);

        registered = true;
    }


}
