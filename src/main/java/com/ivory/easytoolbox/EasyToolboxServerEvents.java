package com.ivory.easytoolbox;

import com.ivory.easytoolbox.newtoolbox.InventoryToolboxLogic;
import com.ivory.easytoolbox.newtoolbox.InventoryToolboxTicker;
import com.ivory.easytoolbox.newtoolbox.NewToolboxHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EasyToolbox.MODID)
public class EasyToolboxServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player)
            InventoryToolboxTicker.tick(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        CompoundTag data = player.getPersistentData().getCompound(InventoryToolboxLogic.DATA_KEY);
        if (!data.isEmpty())
            NewToolboxHandler.syncData(player);
    }
}
