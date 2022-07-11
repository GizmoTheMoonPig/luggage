package com.gizmo.luggage.network;

import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.client.ClientEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class OpenLuggageScreenPacket {

	private final int containerId;
	private final int entityId;

	public OpenLuggageScreenPacket(int containerId, int entityId) {
		this.containerId = containerId;
		this.entityId = entityId;
	}

	public OpenLuggageScreenPacket(FriendlyByteBuf buf) {
		this.containerId = buf.readUnsignedByte();
		this.entityId = buf.readInt();
	}

	public FriendlyByteBuf encode() {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeByte(this.containerId);
		buf.writeInt(this.entityId);
		return buf;
	}

	public static ResourceLocation getID() {
		return new ResourceLocation(Luggage.ID, "open_luggage_screen_packet");
	}

	public static class Handler {

		public static boolean onMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			OpenLuggageScreenPacket message = new OpenLuggageScreenPacket(buf);
			client.execute(() -> {
				ClientEvents.handlePacket(message);
			});
			return true;
		}
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
