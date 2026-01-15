package com.viquelle.examplemod.client.light.modifiers;

import com.viquelle.examplemod.client.light.AbstractLight;
import net.minecraft.util.Mth;

import java.util.Random;

public class FlickerModifier implements LightModifier {
    private final Random random = new Random();
    private final float speed;
    private final float intensity;
    private final float flashChance;
    private float internalTime = 0f;

    public FlickerModifier(float speed, float intensity, float flashChance) {
        this.speed = speed;
        this.intensity = intensity;
        this.flashChance = flashChance;
    }

    @Override
    public Type getType() {
        return Type.MULTIPLICATIVE;
    }

    @Override
    public float getDuration() {
        return -1;
    }

    @Override
    public float getValue(AbstractLight<?> light, float deltaTIme, float progress) {
        internalTime += deltaTIme;
        float t = internalTime * speed;

        float slowWave = Mth.sin(t * 1.5f) * 0.4f;

        float mediumWave = Mth.sin(t * 12.0f) * 0.3f;

        float fastWave = Mth.sin(t * 25.0f) * 0.2f;

        float crackle = 0;
        if (random.nextFloat() > (1 - flashChance)) {
            crackle = (random.nextFloat() - 0.5f);
        }

        float total = (slowWave + mediumWave + fastWave + crackle);
        return 1.0f + total * intensity;
    }
}
