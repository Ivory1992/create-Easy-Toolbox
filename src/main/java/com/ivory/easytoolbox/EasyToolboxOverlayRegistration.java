package com.ivory.easytoolbox;

import com.ivory.easytoolbox.newtoolbox.NewToolboxHandlerClient;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = EasyToolbox.MODID, value = Dist.CLIENT)
public class EasyToolboxOverlayRegistration {

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(EasyToolbox.MODID, "toolbox"),
                NewToolboxHandlerClient.OVERLAY);
    }
}
