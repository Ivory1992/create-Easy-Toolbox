package com.ivory.easytoolbox.newtoolbox;

import com.ivory.easytoolbox.EasyToolbox;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NewDisposePacket(int toolboxInvSlot) implements CustomPacketPayload {
    public static final Type<NewDisposePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EasyToolbox.MODID, "dispose_all"));

    public static final StreamCodec<ByteBuf, NewDisposePacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(NewDisposePacket::new, NewDisposePacket::toolboxInvSlot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NewDisposePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
                InventoryToolboxLogic.disposeAll(player, packet.toolboxInvSlot);
        });
    }
}

