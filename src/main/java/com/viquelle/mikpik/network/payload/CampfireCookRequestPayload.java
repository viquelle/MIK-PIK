package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CampfireCookRequestPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<CampfireCookRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "cook_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CampfireCookRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CampfireCookRequestPayload::pos,
            CampfireCookRequestPayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

