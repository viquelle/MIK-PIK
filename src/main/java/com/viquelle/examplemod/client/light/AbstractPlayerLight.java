package com.viquelle.examplemod.client.light;

import com.viquelle.examplemod.ExampleMod;

public abstract class AbstractPlayerLight implements IPlayerLight {
    protected boolean active = false;
    protected int color = 0xAAAAFF;
    protected float fuel;
    protected float brigtness = 0f;

    @Override
    public boolean isActive() {
        return active;
    }

    public void consumeFuel(float delta) {
        fuel = Math.max(0,fuel - delta);
        if (fuel == 0) ExampleMod.LOGGER.info("[CONSUMEFUEL] fuel is 0");
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }
}
