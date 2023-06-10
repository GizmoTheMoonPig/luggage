package com.gizmo.luggage.network;

import com.gizmo.luggage.entity.AbstractLuggage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SitNearbyLuggagesPacket {
	public SitNearbyLuggagesPacket() {}

	public SitNearbyLuggagesPacket(FriendlyByteBuf buf) {}

	public void encode(FriendlyByteBuf buf) {}

	public static class Handler {
		public static void onMessage(SitNearbyLuggagesPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player != null) {
					List<AbstractLuggage> nearbyOwnedLuggages = player.level().getEntitiesOfClass(AbstractLuggage.class, player.getBoundingBox().inflate(8.0F), entity -> entity.getOwner() == player);
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
