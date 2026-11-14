package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NewToolboxHandler {

    static int validationTimer = 20;

    public static void entityTick(Entity entity, Level world) {
        if (world.isClientSide)
            return;
        if (!(world instanceof ServerLevel))
            return;
        if (!(entity instanceof ServerPlayer player))
            return;
        if (entity.tickCount % validationTimer != 0)
            return;

        if (!player.getPersistentData()
                .contains("CreateToolboxData"))
            return;

        boolean sendData = false;
        CompoundTag compound = player.getPersistentData()
                .getCompound("CreateToolboxData");
        for (int i = 0; i < 9; i++) {
            String key = String.valueOf(i);
            if (!compound.contains(key))
                continue;

            CompoundTag data = compound.getCompound(key);
            BlockPos pos = NBTHelper.readBlockPos(data, "Pos");
            int slot = data.getInt("Slot");

            if (!world.isLoaded(pos))
                continue;
            if (!(world.getBlockState(pos)
                    .getBlock() instanceof ToolboxBlock)) {
                compound.remove(key);
                sendData = true;
                continue;
            }

            BlockEntity prevBlockEntity = world.getBlockEntity(pos);
            if (prevBlockEntity instanceof ToolboxBlockEntity)
                ((ToolboxBlockEntity) prevBlockEntity).connectPlayer(slot, player, i);
        }

        if (sendData)
            syncData(player);
    }

    public static void playerLogin(Player player) {
        if (!(player instanceof ServerPlayer))
            return;
        if (player.getPersistentData()
                .contains("CreateToolboxData")
                && !player.getPersistentData()
                .getCompound("CreateToolboxData")
                .isEmpty()) {
            syncData(player);
        }
    }

    public static void syncData(Player player) {
        CatnipServices.NETWORK.sendToClient((ServerPlayer) player,
                new ISyncPersistentData.PersistentDataPacket(player));
    }

    public static void unequip(Player player, int hotbarSlot, boolean keepItems) {
        CompoundTag compound = player.getPersistentData()
                .getCompound("CreateToolboxData");
        Level world = player.level();
        String key = String.valueOf(hotbarSlot);
        if (!compound.contains(key))
            return;

        CompoundTag prevData = compound.getCompound(key);
        BlockPos prevPos = NBTHelper.readBlockPos(prevData, "Pos");
        int prevSlot = prevData.getInt("Slot");

        BlockEntity prevBlockEntity = world.getBlockEntity(prevPos);
        if (prevBlockEntity instanceof ToolboxBlockEntity toolbox) {
            toolbox.unequip(prevSlot, player, hotbarSlot, keepItems || !ToolboxHandler.withinRange(player, toolbox));
        }
        compound.remove(key);
    }

}
