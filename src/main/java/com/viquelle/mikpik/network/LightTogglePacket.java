package com.viquelle.mikpik.network;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.AbstractLightItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LightTogglePacket(int slot, boolean enabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<LightTogglePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "light_toogle"));

    public static final StreamCodec<ByteBuf, LightTogglePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LightTogglePacket::slot,
            ByteBufCodecs.BOOL, LightTogglePacket::enabled,
            LightTogglePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.getItem() instanceof AbstractLightItem) {
                AbstractLightItem.toggleTo(stack, enabled);
            }

        });
    }
}
