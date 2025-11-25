package com.viquelle.examplemod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public class SanityData {
    private float sanity;
    private Map<String, Integer> eventCooldowns;
    public SanityData(float sanity, Map<String, Integer> eventCooldowns) {
        this.sanity = sanity;
        this.eventCooldowns = eventCooldowns;
    }

    // Значение по умолчанию
    public static final SanityData DEFAULT = new SanityData(100.0f, new HashMap<>());

    // Codec для сохранения (NBT)
    public static final Codec<SanityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("sanity").forGetter(SanityData::getSanity),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("eventCooldowns").forGetter(SanityData::getEventCooldowns)
    ).apply(instance, SanityData::new));

    // StreamCodec для синхронизации (сеть)
//    public static final StreamCodec<ByteBuf, SanityData> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.FLOAT, SanityData::getSanity,
//            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), SanityData::getEventCooldowns,
//            SanityData::new
//    );

    // Геттеры (для RecordCodecBuilder)
    public float getSanity() { return sanity; }
    public void setSanity(float var) { sanity = Math.max(0,Math.min(var,100)); }
    public Map<String, Integer> getEventCooldowns() { return eventCooldowns; }
}