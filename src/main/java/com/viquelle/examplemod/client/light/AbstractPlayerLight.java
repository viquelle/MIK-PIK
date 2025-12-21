package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class AbstractPlayerLight<T extends LightData> implements IPlayerLight{
    protected boolean active = false;

    protected int color = 0xFFFFFF;
    protected float currentBrightness = 0.0f;
    protected float maxBrightness = 1.0f;

    protected LightRenderHandle<T> handle;
    protected T light;

    protected List<CurveSegment> curves;

    public float computeBrightness(float t) {
        for (CurveSegment seg : curves) {
            if (t >= seg.startTick() && t <= seg.endTick()) {
                float x = (float)(t - seg.startTick()) / (float)(seg.endTick() - seg.startTick());

                return lerp(seg.from(), seg.to(), applyCurve(x, seg.curve()));
            }
        }
        return 0f;
    }

    protected float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    protected float applyCurve(float x, LightCurve c) {
        return switch (c) {
            case LINEAR -> x;
            case EASE_IN -> x * x;
            case EASE_OUT -> 1 - (1 - x) * (1 - x);
            case EASE_IN_OUT -> x < 0.5f
                    ? 2 * x * x
                    : 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
            case FLICKER ->
                    x + ((float) Math.random() - 0.5f) * 0.1f;
        };
    }

}
