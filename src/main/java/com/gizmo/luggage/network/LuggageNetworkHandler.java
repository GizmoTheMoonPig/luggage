package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class LuggageNetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(LuggageMod.ID, "channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, CallLuggagePetsPacket.class, CallLuggagePetsPacket::encode, CallLuggagePetsPacket::new, CallLuggagePetsPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, OpenLuggageScreenPacket.class, OpenLuggageScreenPacket::encode, OpenLuggageScreenPacket::new, OpenLuggageScreenPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, SitNearbyLuggagesPacket.class, SitNearbyLuggagesPacket::encode, SitNearbyLuggagesPacket::new, SitNearbyLuggagesPacket.Handler::onMessage);
	}
}
