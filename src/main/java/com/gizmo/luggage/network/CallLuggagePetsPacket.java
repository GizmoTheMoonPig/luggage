package com.gizmo.luggage.network;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CallLuggagePetsPacket {

	private final int playerId;

	public CallLuggagePetsPacket(int playerId) {
		this.playerId = playerId;
	}

	public CallLuggagePetsPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
	}

	public static class Handler {
		public static void onMessage(CallLuggagePetsPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player != null) {
					player.getLevel().getAllEntities().forEach(luggageIHope -> {
						if (luggageIHope instanceof LuggageEntity luggage && luggage.getOwner() != null && luggage.getOwner().is(Objects.requireNonNull(player.getLevel().getEntity(message.playerId)))) {
							luggage.stopRiding();
							luggage.moveTo(player.position());
							if (luggage.isTryingToFetchItem()) luggage.setTryingToFetchItem(false);
							// 10 second cooldown between trying to fetch items
							luggage.setFetchCooldown(200);
							luggage.setInSittingPose(false);
						}
					});
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}
