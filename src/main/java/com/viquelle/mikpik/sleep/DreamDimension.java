package com.viquelle.mikpik.sleep;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class DreamDimension {
    public static final ResourceKey<Level> DAY = create("dream_day");
    public static final ResourceKey<Level> NIGHT_NEW_MOON = create("dream_night_new_moon");
    public static final ResourceKey<Level> NIGHT_FULL_MOON = create("dream_night_full_moon");

    private static ResourceKey<Level> create(String name) {
        return ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(
                        MikpikMod.MODID,
                        name
                )
        );
    }

}