package com.gizmo.luggage.network;

import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.entity.LuggageEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class CallLuggagePetsPacket {

	private int playerId;

	public CallLuggagePetsPacket(int playerId) {
		this.playerId = playerId;
	}

	public CallLuggagePetsPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
	}

	public FriendlyByteBuf encode() {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(this.playerId);
		return buf;
	}

	public static ResourceLocation getID() {
		return new ResourceLocation(Luggage.ID, "call_luggage_packet");
	}

	public static class Handler {
		public static boolean onMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			CallLuggagePetsPacket message = new CallLuggagePetsPacket(buf);
			server.execute(() -> {
				if(player != null) {
					player.getLevel().getAllEntities().forEach(luggageIHope -> {
						if(luggageIHope instanceof LuggageEntity luggage && luggage.getOwner() != null && luggage.getOwner().is(player.getLevel().getEntity(message.playerId))) {
							luggage.moveTo(player.position());
							if(luggage.isTryingToFetchItem()) luggage.setTryingToFetchItem(false);
						}
					});
				}
			});
			return true;
		}
	}
}
