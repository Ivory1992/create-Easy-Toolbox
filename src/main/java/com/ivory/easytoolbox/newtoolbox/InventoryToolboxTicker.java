package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InventoryToolboxTicker {
    private InventoryToolboxTicker() {}

    public static void tick(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(InventoryToolboxLogic.DATA_KEY))
            return;
        CompoundTag all = root.getCompound(InventoryToolboxLogic.DATA_KEY);
        if (all.isEmpty())
            return;

        Inventory inv = player.getInventory();
        Map<Integer, ItemToolboxSource> boxes = new HashMap<>();
        Set<Integer> loaded = new HashSet<>();
        Set<ItemToolboxSource> dirty = new HashSet<>();
        boolean sendData = false;

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            String key = String.valueOf(hotbar);
            if (!all.contains(key))
                continue;

            CompoundTag entry = all.getCompound(key);
            int invSlot = entry.getInt("InvSlot");
            int compartment = entry.getInt("Slot");

            if (loaded.add(invSlot)) {
                ItemToolboxSource src = ItemToolboxSource.of(player, invSlot);
                if (src != null)
                    boxes.put(invSlot, src);
            }
            ItemToolboxSource box = boxes.get(invSlot);

            // toolbox moved/gone, or bad data: detach
            if (box == null || invSlot == hotbar || compartment < 0 || compartment >= 8) {
                all.remove(key);
                sendData = true;
                continue;
            }

            ItemStack reference = box.filter(compartment);
            ItemStack playerStack = inv.getItem(hotbar);

            if (reference.isEmpty() || (!playerStack.isEmpty()
                    && !ToolboxInventory.canItemsShareCompartment(playerStack, reference))) {
                all.remove(key);
                sendData = true;
                continue;
            }

            int count = playerStack.getCount();
            int target = (reference.getMaxStackSize() + 1) / 2;

            if (count < target) {
                ItemStack extracted = box.inventory().takeFromCompartment(target - count, compartment, false);
                if (!extracted.isEmpty()) {
                    dirty.add(box);
                    ItemStack template = playerStack.isEmpty() ? extracted : playerStack;
                    inv.setItem(hotbar, template.copyWithCount(count + extracted.getCount()));
                }
            }

            if (count > target) {
                int surplus = count - target;
                ItemStack toDistribute = playerStack.copyWithCount(surplus);
                int deposited = surplus - box.inventory().distributeToCompartment(toDistribute, compartment, false).getCount();
                if (deposited > 0) {
                    dirty.add(box);
                    inv.setItem(hotbar, playerStack.copyWithCount(count - deposited));
                }
            }
        }

        for (ItemToolboxSource box : dirty)
            box.commit();
        if (sendData)
            NewToolboxHandler.syncData(player);
    }
}
