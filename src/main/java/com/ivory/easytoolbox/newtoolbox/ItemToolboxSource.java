package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.simibubi.create.content.equipment.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

public class ItemToolboxSource {
    private final int invSlot;
    private final ItemStack stack;
    private final ToolboxInventory inventory;
    private final DyeColor color;

    private ItemToolboxSource(int invSlot, ItemStack stack, ToolboxInventory inventory, DyeColor color) {
        this.invSlot = invSlot;
        this.stack = stack;
        this.inventory = inventory;
        this.color = color;
    }

    @Nullable
    public static ItemToolboxSource of(Player player, int invSlot) {
        Inventory inv = player.getInventory();
        if (invSlot < 0 || invSlot >= inv.getContainerSize())
            return null;
        ItemStack stack = inv.getItem(invSlot);
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof ToolboxBlock box))
            return null;
        ToolboxInventory stored = stack.get(AllDataComponents.TOOLBOX_INVENTORY);
        return new ItemToolboxSource(invSlot, stack, copyOf(stored), box.getColor());
    }

    static ToolboxInventory copyOf(@Nullable ToolboxInventory src) {
        ToolboxInventory copy = new ToolboxInventory(null);
        if (src == null)
            return copy;
        List<ItemStack> filters = ToolboxAccess.filters(copy);
        List<ItemStack> srcFilters = ToolboxAccess.filters(src);
        for (int i = 0; i < 8 && i < srcFilters.size(); i++)
            filters.set(i, srcFilters.get(i).copy());
        for (int i = 0; i < src.getSlots(); i++)
            copy.setStackInSlot(i, src.getStackInSlot(i).copy());
        return copy;
    }

    public int invSlot() {
        return invSlot;
    }

    public DyeColor color() {
        return color;
    }

    public Component displayName() {
        return stack.getHoverName();
    }

    public ToolboxInventory inventory() {
        return inventory;
    }

    public ItemStack filter(int compartment) {
        return ToolboxAccess.filters(inventory).get(compartment);
    }

    public boolean compartmentEmpty(int compartment) {
        return inventory.getStackInSlot(compartment * STACKS_PER_COMPARTMENT).isEmpty();
    }

    public void commit() {
        stack.set(AllDataComponents.TOOLBOX_INVENTORY, copyOf(inventory));
    }
}

