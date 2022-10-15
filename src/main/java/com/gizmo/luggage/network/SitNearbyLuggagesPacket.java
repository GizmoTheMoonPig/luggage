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

import java.util.List;

public class SitNearbyLuggagesPacket {

	public static FriendlyByteBuf encode(final int playerId) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(playerId);
		return buf;
	}

	public static int decode(FriendlyByteBuf buf) {
		return buf.readInt();
	}

	public static ResourceLocation getID() {
		return new ResourceLocation(Luggage.ID, "sit_nearby_luggage_packet");
	}

	public static class Handler {
		public static boolean onMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			server.execute(() -> {
				if (player != null) {
					List<LuggageEntity> nearbyOwnedLuggages = player.getLevel().getEntitiesOfClass(LuggageEntity.class, player.getBoundingBox().inflate(8.0F), entity -> entity.getOwner() == player);
					if (!nearbyOwnedLuggages.isEmpty()) {
						for (LuggageEntity luggage : nearbyOwnedLuggages) {
							luggage.setChilling(!luggage.isChilling());
						}
					}
				}
			});
			return true;
		}
	}
}
