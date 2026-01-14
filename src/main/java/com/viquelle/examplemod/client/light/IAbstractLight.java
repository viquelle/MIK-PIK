package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.light.data.LightData;

public interface IAbstractLight<T extends LightData> {
    void tick(float deltaTick, float partialTick);
    void unregister();
    void register();
    float epsilon = 0.001f;

}
