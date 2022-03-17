package com.gizmo.luggage.network;

import com.gizmo.luggage.Luggage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class LuggageNetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Luggage.ID, "channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int id = 0;
		CHANNEL.messageBuilder(OpenLuggageScreenPacket.class, id++).encoder(OpenLuggageScreenPacket::encode).decoder(OpenLuggageScreenPacket::new).consumer(OpenLuggageScreenPacket.Handler::onMessage).add();
	}
}
