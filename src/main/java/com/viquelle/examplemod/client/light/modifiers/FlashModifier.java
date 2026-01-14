package com.viquelle.examplemod.client.light.modifiers;

import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.LightCurve;

public class FlashModifier extends ActiveModifier {
    private final float power;
    private final float duration;;
    private final LightCurve curve = LightCurve.EASE_OUT;

    public FlashModifier(LightModifier modifier, float power, float duration) {
        super(modifier);
        this.power = power;
        this.duration = duration;
    }

}
