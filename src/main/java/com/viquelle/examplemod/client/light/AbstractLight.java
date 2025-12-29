package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractLight<T extends LightData> implements IPlayerLight{
    protected boolean active = false;

    protected int color = 0xFFFFFF;
    protected float currentIntensivity;
    protected float targetIntensivity;

    protected LightRenderHandle<T> handle;
    protected T light;
    private Map<String, CurveSegment> curves = new HashMap<>();

    protected void bob() {
        curves.
    }

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
