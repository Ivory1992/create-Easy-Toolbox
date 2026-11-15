package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.AllKeys;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
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

        List<ItemToolboxSource> boxes = findToolboxes(player);

        CompoundTag data = player.getPersistentData().getCompound("EasyToolboxData");

        String hotbarKey = String.valueOf(player.getInventory().selected);

        if (data.contains(hotbarKey)) {
            CompoundTag entry = data.getCompound(hotbarKey);
            int invSlot = entry.getInt("InvSlot");
            ItemToolboxSource src = ItemToolboxSource.of(player, invSlot);
            if (src != null) {
                var screen = new NewRadialToolboxMenu(boxes, NewRadialToolboxMenu.State.SELECT_ITEM_UNEQUIP, src);
                screen.prevSlot(entry.getInt("Slot"));
                ScreenOpener.open(screen);
            } else {
                ScreenOpener.open(new NewRadialToolboxMenu(List.of(), NewRadialToolboxMenu.State.DETACH, null));
            }
            return;
        }

        if (boxes.isEmpty())
            return;

        if (boxes.size() == 1)
            ScreenOpener.open(new NewRadialToolboxMenu(boxes, NewRadialToolboxMenu.State.SELECT_ITEM, boxes.get(0)));
        else
            ScreenOpener.open(new NewRadialToolboxMenu(boxes, NewRadialToolboxMenu.State.SELECT_BOX, null));
    }

    private static List<ItemToolboxSource> findToolboxes(Player player) {
        List<ItemToolboxSource> out = new ArrayList<>();
        Inventory inv = player.getInventory();
        int selected = inv.selected;
        for (int i = 0; i < inv.items.size(); i++) {
            if (i == selected)
                continue;
            ItemToolboxSource src = ItemToolboxSource.of(player, i);
            if (src != null) out.add(src);
        }
        return out;
    }
}
