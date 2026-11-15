package com.ivory.easytoolbox;

import com.ivory.easytoolbox.newtoolbox.NewToolboxHandlerClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EasyToolbox.MODID, value = Dist.CLIENT)
public class EasyToolboxClientEvents {

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS)
            return;
        if (Minecraft.getInstance().screen != null)
            return;
        NewToolboxHandlerClient.onKeyInput(event.getKey(), true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        NewToolboxHandlerClient.clientTick();
    }
}

