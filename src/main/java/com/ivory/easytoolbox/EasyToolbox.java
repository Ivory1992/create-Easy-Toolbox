package com.ivory.easytoolbox;

import com.ivory.easytoolbox.newtoolbox.NewDisposePacket;
import com.ivory.easytoolbox.newtoolbox.NewEquipPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(EasyToolbox.MODID)
public class EasyToolbox {
    public static final String MODID = "easytoolbox";

    public EasyToolbox(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(EasyToolbox::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(NewEquipPacket.TYPE, NewEquipPacket.STREAM_CODEC, NewEquipPacket::handle);
        registrar.playToServer(NewDisposePacket.TYPE, NewDisposePacket.STREAM_CODEC, NewDisposePacket::handle);
    }
}
