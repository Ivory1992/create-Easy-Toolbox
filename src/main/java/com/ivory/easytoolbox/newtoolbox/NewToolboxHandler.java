package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class NewToolboxHandler {

    static int validationTimer = 20;

    public static void playerLogin(Player player) {
        if (!(player instanceof ServerPlayer))
            return;
        if (player.getPersistentData()
                .contains("EasyToolboxData")
                && !player.getPersistentData()
                .getCompound("EasyToolboxData")
                .isEmpty()) {
            syncData(player);
        }
    }

    public static void syncData(Player player) {
        CatnipServices.NETWORK.sendToClient((ServerPlayer) player,
                new ISyncPersistentData.PersistentDataPacket(player));
    }
}
