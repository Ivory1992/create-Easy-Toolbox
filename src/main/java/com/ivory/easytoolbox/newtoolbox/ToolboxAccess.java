package com.ivory.easytoolbox.newtoolbox;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.List;


public class ToolboxAccess {

    private static final Field FILTERS = field(ToolboxInventory.class, "filters");
    private static final Field BE_INVENTORY = field(ToolboxBlockEntity.class, "inventory");

    private static Field field(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Create changed field " + owner.getSimpleName() + "." + name,e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ItemStack> filters(ToolboxInventory inv) {
        try {
            return (List<ItemStack>) FILTERS.get(inv);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ToolboxInventory inventory(ToolboxBlockEntity be) {
        try {
            return (ToolboxInventory) BE_INVENTORY.get(be);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
