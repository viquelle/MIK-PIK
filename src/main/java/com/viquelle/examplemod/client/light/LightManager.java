package com.viquelle.examplemod.client.light;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import java.util.ArrayList;
import java.util.List;

public class LightManager {
    private static final List<AbstractPlayerLight<?>> lights = new ArrayList<>();

    public static void register(AbstractPlayerLight<?> light) {
        lights.add(light);
    }

    public static void tickLocalPlayer(float pt) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;

        for (IPlayerLight light : lights) {
            light.update(p,pt);
        }
    }

    public static void clearAll() {
        lights.forEach(IPlayerLight::remove);
        lights.clear();
    }
}
