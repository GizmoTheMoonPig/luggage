package com.gizmo.luggage.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class LuggageNetworkHandler {
	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(CallLuggagePetsPacket.getID(), CallLuggagePetsPacket.Handler::onMessage);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(OpenLuggageScreenPacket.getID(), OpenLuggageScreenPacket.Handler::onMessage);
		}
	}
}
