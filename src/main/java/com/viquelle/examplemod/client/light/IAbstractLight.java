package com.viquelle.examplemod.client.light;

import net.minecraft.world.entity.player.Player;

public interface IPlayerLight {
    boolean isDirty = true;
    IPlayerLight create();
    void tick(Player player, float partialTick);
    void remove();
}
