package com.ivory.easytoolbox.newtoolbox;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.toolbox.*;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Comparator;
import java.util.List;


public class NewToolboxHandlerClient {

    static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !AllKeys.TOOLBELT.isPressed())
            COOLDOWN--;
    }

    public static void onKeyInput(int key, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        if (!AllKeys.TOOLBELT.doesModifierAndCodeMatch(key))
            return;
        if (COOLDOWN > 0)
            return;
        LocalPlayer player = mc.player;
        if (player == null)
            return;
        Level level = player.level();

        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);
        toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));

        CompoundTag compound = player.getPersistentData()
                .getCompound("CreateToolboxData");

        String slotKey = String.valueOf(player.getInventory().selected);
        boolean equipped = compound.contains(slotKey);

        if (equipped) {
            BlockPos pos = NBTHelper.readBlockPos(compound.getCompound(slotKey), "Pos");
            double max = ToolboxHandler.getMaxRange(player);
            boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < max * max;

            if (canReachToolbox) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ToolboxBlockEntity) {
                    RadialToolboxMenu screen = new RadialToolboxMenu(toolboxes,
                            RadialToolboxMenu.State.SELECT_ITEM_UNEQUIP, (ToolboxBlockEntity) blockEntity);
                    screen.prevSlot(compound.getCompound(slotKey)
                            .getInt("Slot"));
                    ScreenOpener.open(screen);
                    return;
                }
            }

            ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.DETACH, null));
            return;
        }

        if (toolboxes.isEmpty())
            return;

        if (toolboxes.size() == 1)
            ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_ITEM, toolboxes.get(0)));
        else
            ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_BOX, null));
    }

}
