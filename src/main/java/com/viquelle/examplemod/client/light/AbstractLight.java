package com.viquelle.examplemod.client.light;

import com.viquelle.examplemod.client.light.modifiers.ActiveModifier;
import com.viquelle.examplemod.client.light.modifiers.LightModifier;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractLight<T extends LightData> implements IAbstractLight<T> {

    public enum State {
        TURNING_ON,
        ON,
        TURNING_OFF,
        OFF,
    }

    protected final Player player;
    protected final int color;
    protected final float minBright;
    protected final float maxBright;
    protected final float speedOn;
    protected final float speedOff;
    protected final LightCurve curveOn;
    protected final LightCurve curveOff;
    protected final boolean isNegative;
    protected boolean isPermanent = false;
    protected float lastSentBrightness = -1f;

    protected State state = State.TURNING_ON;
    protected float progress = 0f;    // 0.0 (OFF) <-> 1.0 (ON)
    protected float lifeTime = 0f;
    protected float currentBrightness;

    protected LightRenderHandle<T> handle = null;
    private final List<ActiveModifier> activeModifiers = new ArrayList<>();

    public void setPermanent(boolean permanent) {
        this.isPermanent = permanent;
    }

    protected AbstractLight(Builder<?> builder) {
        this.player = builder.player;
        this.color = builder.color;
        this.minBright = builder.minBrightness;
        this.maxBright = builder.maxBrightness;
        this.speedOn = builder.deltaSpeed;
        this.speedOff = builder.deltaSpeedOff;
        this.curveOn = builder.curveOn;
        this.curveOff = builder.curveOff;
        this.isNegative = builder.isNegative;
        this.currentBrightness = minBright;
    }

    public void addModifier(LightModifier modifier) {
        this.activeModifiers.add(new ActiveModifier(modifier));
    }
    @Override
    public void tick(float deltaTime, float partialTick) {
        if (handle == null) return;
        lifeTime += deltaTime;

        updateProgress(deltaTime);

        LightCurve currentCurve = (state == State.TURNING_OFF || state == State.OFF) ? curveOff : curveOn;
        float baseBrightness = minBright + (maxBright - minBright) * currentCurve.apply(progress);

        float multiplier = 1.0f;
        float additive = 0.0f;

        if (!activeModifiers.isEmpty()) {
            Iterator<ActiveModifier> it = activeModifiers.iterator();
            while (it.hasNext()) {
                ActiveModifier active = it.next();
                if (!active.tick(deltaTime)) {
                    active.getModifier().onExpire(this);
                    it.remove();
                    continue;
                }
                float val = active.getModifier().getValue(this, deltaTime, active.getProgress());
                if (active.getModifier().getType() == LightModifier.Type.MULTIPLICATIVE) {
                    multiplier *= val;
                } else {
                    additive += val;
                }
            }
        }

        if (state == State.OFF) {
            this.currentBrightness = minBright;
        } else {
            this.currentBrightness = (baseBrightness * multiplier) + additive;
        }

        if (Math.abs(this.currentBrightness - lastSentBrightness) > 0.001f) {
            float renderValue = isNegative ? -this.currentBrightness : this.currentBrightness;
            handle.getLightData().setBrightness(renderValue);
            handle.getLightData().setColor(this.color);
            this.lastSentBrightness = this.currentBrightness;
        }
    }

    private void updateProgress(float deltaTime) {
        switch (state) {
            case TURNING_ON -> {
                float step = (speedOn <= 0) ? 1.0f : deltaTime / speedOn;
                progress = Math.min(1f, progress + step);
                if (progress >= 1f) state = State.ON;
            }
            case TURNING_OFF -> {
                float step = (speedOff <= 0) ? 1.0f : deltaTime / speedOff;
                progress = Math.max(0f, progress - step);
                if (progress <= 0f) state = State.OFF;
            }
            case ON -> progress = 1f;
            case OFF -> progress = 0f;
        }
    }

    public void turnOn() {
        if (state != State.ON) state = State.TURNING_ON;
    }

    public void turnOff() {
        if (state != State.OFF) state = State.TURNING_OFF;
    }

    public boolean isDead() {
        if (this.isPermanent) return false;
        return state == State.OFF;
    }

    public void unregister() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            if (handle == null) return;
            handle.free();
            handle = null;
        });
    }

    public static abstract class Builder<B extends Builder<B>> {
        protected final Player player;
        protected int color = 0xFFFFFF;
        protected float maxBrightness = 1.0f;
        protected float minBrightness = 0.0f;
        protected float deltaSpeed = 0.4f;
        protected float deltaSpeedOff = 0.4f;
        protected LightCurve curveOn = LightCurve.LINEAR;
        protected LightCurve curveOff = LightCurve.LINEAR;
        protected boolean isNegative = false;

        public Builder(Player player) { this.player = player; }

        @SuppressWarnings("unchecked")
        public B setColor(int c) { this.color = c; return (B)this; }
        @SuppressWarnings("unchecked")
        public B setBrightness(float min, float max) { this.minBrightness = min; this.maxBrightness = max; return (B)this; }
        @SuppressWarnings("unchecked")
        public B setSpeeds(float on, float off) { this.deltaSpeed = on; this.deltaSpeedOff = off; return (B)this; }
        @SuppressWarnings("unchecked")
        public B setCurves(LightCurve on, LightCurve off) { this.curveOn = on; this.curveOff = off; return (B)this; }
        @SuppressWarnings("unchecked")
        public B setNegative(boolean n) { this.isNegative = n; return (B)this; }

        public abstract AbstractLight<?> build();
    }

    public float getBrightness() { return currentBrightness; }
    public float getMinBright() { return minBright; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Player getPlayer() { return player; }
}