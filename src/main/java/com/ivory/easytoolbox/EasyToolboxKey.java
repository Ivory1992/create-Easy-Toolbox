package com.ivory.easytoolbox;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EasyToolbox.MODID, value = Dist.CLIENT)
public class EasyToolboxKey {
    public static final KeyMapping INVENTORY_TOOLBOX = new KeyMapping("Key.easytoolbox.inventory_toolbox",
            GLFW.GLFW_KEY_RIGHT_ALT, "key.categories.easytoolbox");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(INVENTORY_TOOLBOX);
    }
}
