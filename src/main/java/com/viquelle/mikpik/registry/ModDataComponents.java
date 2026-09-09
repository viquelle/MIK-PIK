package com.viquelle.mikpik.registry;

import com.mojang.serialization.Codec;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(MikpikMod.MODID);
    public static final String charge_ticks = "charge_ticks"; // for items charge
    public static final String last_update = "last_update"; // for items charge
    public static final String charging_start = "charge_start"; // for items charge
    public static final String heart_charge = "heart_charge";
    public static final String spoil_time = "spoil_time";
    public static final String spoil_time_remaining = "spoil_time_remaining";
    public static final String last_reduction = "spoil_last_reduction";

    public static final Supplier<DataComponentType<Float>> PLUSHY_STORED_SANITY =
            COMPONENTS.registerComponentType(
                    "pibble_stored_sanity",
                    floatBuilder -> floatBuilder
                            .persistent(Codec.FLOAT)
                            .networkSynchronized(ByteBufCodecs.FLOAT)
            );

    public static final Supplier<DataComponentType<String>> PLUSHY_ID =
            COMPONENTS.registerComponentType(
                    "pibble_id",
                    builder -> builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.STRING))
            );

    public static final Supplier<DataComponentType<Integer>> CHARGE_TICKS =
            COMPONENTS.registerComponentType(
                    charge_ticks,
                    integerBuilder -> integerBuilder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.INT))
            );

    public static final Supplier<DataComponentType<Long>> LAST_UPDATE =
            COMPONENTS.registerComponentType(
                    last_update,
                    integerBuilder -> integerBuilder
                            .persistent(Codec.LONG)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.LONG))
            );

    public static final Supplier<DataComponentType<Long>> CHARGING_START =
            COMPONENTS.registerComponentType(
                    charging_start,
                    integerBuilder -> integerBuilder
                            .persistent(Codec.LONG)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.LONG))
            );

    public static final Supplier<DataComponentType<Float>> HEART_CHARGE =
            COMPONENTS.registerComponentType(
                    heart_charge,
                    integerBuilder -> integerBuilder
                            .persistent(Codec.FLOAT)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.FLOAT))
            );

    public static final Supplier<DataComponentType<Integer>> SPOIL_TIME =
            COMPONENTS.registerComponentType(
                    spoil_time,
                    integerBuilder -> integerBuilder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    public static final Supplier<DataComponentType<Float>> SPOIL_TIME_REMAINING =
            COMPONENTS.registerComponentType(
                    spoil_time_remaining,
                    b -> b
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.fromCodec(Codec.FLOAT))
            );

    public static final Supplier<DataComponentType<Float>> SPOIL_LAST_REDUCTION =
            COMPONENTS.registerComponentType(
                    last_reduction,
                    b -> b
                            .persistent(Codec.FLOAT)
                            .networkSynchronized(ByteBufCodecs.fromCodec(Codec.FLOAT))
            );

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
