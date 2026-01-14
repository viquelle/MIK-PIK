package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.AbstractLight;
import net.minecraft.world.entity.player.Player;

public interface ILightItem {
    AbstractLight<?> createLight(Player player);
}
