package com.gizmo.luggage.network;

import com.gizmo.luggage.entity.AbstractLuggage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SitNearbyLuggagesPacket {
	private final int playerId;

	public SitNearbyLuggagesPacket(int playerId) {
		this.playerId = playerId;
	}

	public SitNearbyLuggagesPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
	}

	public static class Handler {
		public static void onMessage(SitNearbyLuggagesPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player != null) {
					List<AbstractLuggage> nearbyOwnedLuggages = player.getLevel().getEntitiesOfClass(AbstractLuggage.class, player.getBoundingBox().inflate(8.0F), entity -> entity.getOwner() == player);
					if (!nearbyOwnedLuggages.isEmpty()) {
						for (AbstractLuggage luggage : nearbyOwnedLuggages) {
							luggage.setInSittingPose(!luggage.isInSittingPose());
						}
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}
