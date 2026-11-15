package com.ivory.easytoolbox.newtoolbox;

import com.ivory.easytoolbox.EasyToolbox;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NewEquipPacket(int toolboxInvSlot, int slot, int hotbarSlot) implements CustomPacketPayload {
    public static final Type<NewEquipPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EasyToolbox.MODID, "equip"));

    public static final StreamCodec<ByteBuf, NewEquipPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, NewEquipPacket::toolboxInvSlot,
            ByteBufCodecs.VAR_INT, NewEquipPacket::slot,
            ByteBufCodecs.VAR_INT, NewEquipPacket::hotbarSlot,
            NewEquipPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NewEquipPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
                packet.handleOnServer(player);
        });
    }

    private void handleOnServer(ServerPlayer player) {
        if (hotbarSlot < 0 || hotbarSlot > 8)
            return;
        if (toolboxInvSlot < 0) {
            InventoryToolboxLogic.unequip(player, hotbarSlot, false);
            NewToolboxHandler.syncData(player);
            return;
        }
        InventoryToolboxLogic.equip(player, toolboxInvSlot, slot, hotbarSlot);
    }
}
