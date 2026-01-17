package com.viquelle.mikpik.client.light;

import java.util.function.Function;

public enum LightCurve {
    LINEAR(x -> x),
    EASE_IN(x -> x * x),
    EASE_OUT(x -> 1 - (1-x)*(1-x)),
    EASE_IN_OUT(x -> x < 0.5f
            ? 2 * x * x
            : 1 - (float) Math.pow(-2 * x + 2, 2) / 2
            ),
    FLICKER(x -> 0.9f + (float)Math.random() * 0.2f);

    private final Function<Float, Float> function;

    LightCurve(Function<Float, Float> function) {
        this.function = function;
    }

    public float apply(float x) {
        return function.apply(x);
    }
}
