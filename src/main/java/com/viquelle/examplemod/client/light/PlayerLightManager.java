package com.viquelle.examplemod.client.light;

import java.util.List;

public class LightsController {
    private static List<AbstractPlayerLight<?>> lights;

    public static void tick(float partialTick) {
        for (AbstractPlayerLight<?> light : lights) {
            
        }
    }
}
