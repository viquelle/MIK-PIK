package com.viquelle.examplemod.client.light;

import net.minecraft.client.player.LocalPlayer;

public interface IPlayerLight {
    IPlayerLight create();
    void update(LocalPlayer player, float partialTick);
    void remove();
    float fadeTime = 0;
}
