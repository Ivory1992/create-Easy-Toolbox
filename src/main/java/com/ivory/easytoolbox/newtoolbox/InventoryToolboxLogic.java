package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.content.equipment.toolbox.ItemReturnInvWrapper;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;

public final class InventoryToolboxLogic {
    public static final String DATA_KEY = "EasyToolboxData";

    private InventoryToolboxLogic() {}

    public static void equip(ServerPlayer player, int toolboxInvSlot, int compartment, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8)
            return;
        if (toolboxInvSlot == hotbarSlot)
            return;

        unequip(player, hotbarSlot, false);

        ItemToolboxSource box = ItemToolboxSource.of(player, toolboxInvSlot);
        if (box == null || compartment < 0 || compartment >= 8) {
            NewToolboxHandler.syncData(player);
            return;
        }

        Inventory inv = player.getInventory();
        ItemStack playerStack = inv.getItem(hotbarSlot);
        if (!playerStack.isEmpty()
                && !ToolboxInventory.canItemsShareCompartment(playerStack, box.filter(compartment))) {
            box.inventory().inLimitedMode(inventory -> {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, playerStack, false);
                if (!remainder.isEmpty())
                    remainder = ItemHandlerHelper.insertItemStacked(new ItemReturnInvWrapper(inv), remainder, false);
                if (remainder.getCount() != playerStack.getCount())
                    inv.setItem(hotbarSlot, remainder);
            });
            box.commit();
        }

        CompoundTag all = player.getPersistentData().getCompound(DATA_KEY);
        CompoundTag entry = new CompoundTag();
        entry.putInt("Slot", compartment);
        entry.putInt("InvSlot", toolboxInvSlot);
        all.put(String.valueOf(hotbarSlot), entry);
        player.getPersistentData().put(DATA_KEY, all);

        NewToolboxHandler.syncData(player);
    }

    public static void unequip(Player player, int hotbarSlot, boolean keepItems) {
        CompoundTag all = player.getPersistentData().getCompound(DATA_KEY);
        String key = String.valueOf(hotbarSlot);
        if (!all.contains(key))
            return;

        CompoundTag entry = all.getCompound(key);
        if (!keepItems) {
            int compartment = entry.getInt("Slot");
            ItemToolboxSource box = ItemToolboxSource.of(player, entry.getInt("InvSlot"));
            if (box != null && compartment >= 0 && compartment < 8) {
                Inventory inv = player.getInventory();
                ItemStack toInsert = ToolboxInventory.cleanItemNBT(inv.getItem(hotbarSlot).copy());
                ItemStack remainder = box.inventory().distributeToCompartment(toInsert, compartment, false);
                if (remainder.getCount() != toInsert.getCount()) {
                    inv.setItem(hotbarSlot, remainder);
                    box.commit();
                }
            }
        }
        all.remove(key);
    }

    public static void disposeAll(ServerPlayer player, int toolboxInvSlot) {
        ItemToolboxSource box = ItemToolboxSource.of(player, toolboxInvSlot);
        if (box == null)
            return;

        CompoundTag all = player.getPersistentData().getCompound(DATA_KEY);
        MutableBoolean sendData = new MutableBoolean(false);
        MutableBoolean changed = new MutableBoolean(false);
        Inventory inv = player.getInventory();

        box.inventory().inLimitedMode(inventory -> {
            for (int i = 0; i < 36; i++) {
                String key = String.valueOf(i);
                if (all.contains(key) && all.getCompound(key).getInt("InvSlot") == toolboxInvSlot) {
                    unequip(player, i, true);
                    sendData.setTrue();
                }
                if (i == toolboxInvSlot)
                    continue; // don't try to stuff the toolbox into itself
                ItemStack stack = inv.getItem(i);
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, stack, false);
                if (remainder.getCount() != stack.getCount()) {
                    inv.setItem(i, remainder);
                    changed.setTrue();
                }
            }
        });

        if (changed.booleanValue())
            box.commit();
        if (sendData.booleanValue())
            NewToolboxHandler.syncData(player);
    }
}