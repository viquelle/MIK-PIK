package com.viquelle.mikpik.client.light.modifiers;

public class ActiveModifier {
    private final LightModifier modifier;
    private float remainingTime;
    private final float duration;

    public ActiveModifier(LightModifier modifier) {
        this.modifier = modifier;
        this.duration = modifier.getDuration();
        this.remainingTime = this.duration;
    }

    public boolean tick(float deltaTime) {
        if (duration == -1) {
            return true;
        }
        remainingTime -= deltaTime;
        return remainingTime > 0;
    }

    public float getProgress() {
        return Math.max(0f, remainingTime / duration);
    }

    public LightModifier getModifier() { return modifier; }
}