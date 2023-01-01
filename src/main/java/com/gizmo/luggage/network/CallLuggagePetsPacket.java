package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.LuggageEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Objects;

public class CallLuggagePetsPacket {
	public static FriendlyByteBuf encode(final int playerId) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(playerId);
		return buf;
	}

	public static int decode(FriendlyByteBuf buf) {
		return buf.readInt();
	}

	public static ResourceLocation getID() {
		return new ResourceLocation(LuggageMod.ID, "call_luggage_packet");
	}

	public static class Handler {
		public static void onMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			int playerId = decode(buf);
			server.execute(() -> {
				if (player != null) {
					player.getLevel().getAllEntities().forEach(luggageIHope -> {
						if (luggageIHope instanceof LuggageEntity luggage && luggage.getOwner() != null && luggage.getOwner().is(Objects.requireNonNull(player.getLevel().getEntity(playerId)))) {
							luggage.stopRiding();
							luggage.moveTo(player.position());
							if (luggage.isTryingToFetchItem()) luggage.setTryingToFetchItem(false);
							// 10 second cooldown between trying to fetch items
							luggage.setFetchCooldown(200);
							luggage.setForcedToSit(false);
						}
					});
				}
			});
		}
	}
}
