package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

public abstract class AbstractLight<T extends LightData> implements IAbstractLight{
    private int color;
    private float brightness;

    public float lastBrightness = -1f;
    public int lastColor = -1;

    private Player player;
    protected boolean registered = false; // need for known inited in Veil or doesn't
    protected LightRenderHandle<T> handle = null;
    protected boolean isDirty = false;

    protected AbstractLight(Builder builder) {
        this.isDirty = true;
        this.registered = true;
        this.lastColor = builder.color;
        this.lastBrightness = builder.brightness;
        this.player = builder.player;
    }

    protected static class Builder<B extends Builder<B>> {
        private int color = 0xFFFFFF;
        private float brightness = 1.0f;
        private Player player;

        @SuppressWarnings("unchecked")
        public B setColor(int color) {
            this.color = color;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setBrightness(float brightness) {
            this.brightness = brightness;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setPlayer(Player player) {
            this.player = player;
            return (B) this;
        }

        @ApiStatus.OverrideOnly
        public AbstractLight<?> build() {return null;}

    }

    public void syncBrightness(LightRenderHandle<?> handle) {
        brightness = lastBrightness;
        handle.getLightData().setBrightness(brightness);
    }

    public void syncColor(LightRenderHandle<?> handle){
        color = lastColor;
        handle.getLightData().setColor(color);
    }

    public void tick(float pT, LightRenderHandle<?> handle) {
        if (!registered || !isDirty) return;

        if (lastBrightness != brightness) syncBrightness(handle);
        if (lastColor != color) syncColor(handle);
        //isDirty = false;
    }

    public void unregister() {
        handle.free();
        registered = false;
    }

//    protected Map<String, List<CurveSegment>> curves = new HashMap<>();
//    protected int currentCurve = -1;
//    protected void applyCurve() {
//    }
//    protected static class CurveSegment {
//        protected int currentTick = 0;
//        protected int duration;
//        protected LightCurve curve = LightCurve.LINEAR;
//
//        protected void applyCurve() {
//
//        }
//    }
//    protected float lerp(float a, float b, float t) {
//        return a + (b - a) * t;
//    }

//    protected float applyCurve(float x, LightCurve c) {
//        return switch (c) {
//            case LINEAR -> x;
//            case EASE_IN -> x * x;
//            case EASE_OUT -> 1 - (1 - x) * (1 - x);
//            case EASE_IN_OUT -> x < 0.5f
//                    ? 2 * x * x
//                    : 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
//            case FLICKER ->
//                    x + ((float) Math.random() - 0.5f) * 0.1f;
//        };
//    }

}