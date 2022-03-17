package com.gizmo.luggage.network;

import com.gizmo.luggage.client.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(this.containerId);
		buf.writeInt(this.entityId);
	}

	public static class Handler {

		public static boolean onMessage(OpenLuggageScreenPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEvents.handlePacket(message));
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
