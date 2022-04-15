package com.gizmo.luggage.network;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CallLuggagePetsPacket {

	private int playerId;

	public CallLuggagePetsPacket(int playerId) {
		this.playerId = playerId;
	}

	public CallLuggagePetsPacket(PacketBuffer buf) {
		this.playerId = buf.readInt();
	}

	public void encode(PacketBuffer buf) {
		buf.writeInt(this.playerId);
	}

	public static class Handler {
		public static boolean onMessage(CallLuggagePetsPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				if(player != null) {
					player.getLevel().getAllEntities().forEach(luggageIHope -> {
						if(luggageIHope instanceof LuggageEntity) {
							LuggageEntity luggage = (LuggageEntity) luggageIHope;
							if (luggage.getOwner() != null && luggage.getOwner().is(player.getLevel().getEntity(message.playerId))) {
								luggage.moveTo(player.position());
								if (luggage.tryingToFetchItem) luggage.tryingToFetchItem = false;
							}
						}
					});
				}
			});
			return true;
		}
	}
}
