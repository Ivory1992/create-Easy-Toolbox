package com.ivory.easytoolbox.newtoolbox;

import com.ivory.easytoolbox.EasyToolboxKey;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.*;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_OFF;


public class NewToolboxHandlerClient {

    static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !EasyToolboxKey.INVENTORY_TOOLBOX.isDown())
            COOLDOWN--;
    }

    public static void onKeyInput(int key, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        KeyMapping bind = EasyToolboxKey.INVENTORY_TOOLBOX;
        if (key != bind.getKey().getValue())
            return;
        KeyModifier mod = bind.getKeyModifier();
        if (mod != KeyModifier.NONE && !KeyModifier.getActiveModifiers().contains(mod))
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

    public static final LayeredDraw.Layer OVERLAY = NewToolboxHandlerClient::renderOverlay;

    public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR || mc.options.hideGui || mc.gameMode == null)
            return;

        Player player = mc.player;
        CompoundTag data = player.getPersistentData().getCompound("EasyToolboxData");
        if (data.isEmpty())
            return;

        int x = guiGraphics.guiWidth() / 2 - 90;
        int y = guiGraphics.guiHeight() - 23;
        RenderSystem.enableDepthTest();

        for (int slot = 0; slot < 9; slot++) {
            String key = String.valueOf(slot);
            if (!data.contains(key))
                continue;

            int invSlot = data.getCompound(key).getInt("InvSlot");
            ItemStack boxStack = player.getInventory().getItem(invSlot);
            boolean valid = boxStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ToolboxBlock;

            boolean selected = player.getInventory().selected == slot;
            int offset = selected ? 1 : 0;
            AllGuiTextures texture = valid
                    ? (selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON)
                    : (selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF);
            texture.render(guiGraphics, x + 20 * slot - offset, y + offset);
        }
    }
}
