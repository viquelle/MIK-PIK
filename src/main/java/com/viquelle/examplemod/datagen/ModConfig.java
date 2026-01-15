package com.viquelle.examplemod.datagen;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue AMBIENT_BRIGHTNESS = BUILDER
            .comment("How bright is the eyes adaptation light (0.0 to 1.0)")
            .defineInRange("ambientBrightness", 0.3, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
