package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.entity.Luggage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CallLuggagePacket implements CustomPacketPayload {

	public static final Type<CallLuggagePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LuggageMod.ID, "call_nearby_luggage"));
	public static final CallLuggagePacket INSTANCE = new CallLuggagePacket();
	public static final StreamCodec<ByteBuf, CallLuggagePacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(IPayloadContext ctx) {
		if (ctx.flow().isServerbound()) {
			ctx.enqueueWork(() -> {
				Player player = ctx.player();
				ServerLevel level = ((ServerLevel) player.level());
				level.getAllEntities().forEach(luggageIHope -> {
					if (luggageIHope instanceof AbstractLuggage luggage && luggage.getOwner() != null && luggage.getOwner().is(player)) {
						luggage.stopRiding();
						luggage.moveTo(player.position());
						if (luggage instanceof Luggage fetcher) {
							if (fetcher.isTryingToFetchItem()) fetcher.setTryingToFetchItem(false);
							// 10 second cooldown between trying to fetch items
							fetcher.setFetchCooldown(200);
						}
						luggage.setInSittingPose(false);
					}
				});
			});
		}
	}
}
