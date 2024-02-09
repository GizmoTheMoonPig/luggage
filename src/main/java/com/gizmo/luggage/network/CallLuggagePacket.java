package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

public class CallLuggagePacket implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation(LuggageMod.ID, "call_luggage");

	@Override
	public void write(FriendlyByteBuf buf) {

	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static void handle(PlayPayloadContext ctx) {
		if (ctx.flow().isServerbound()) {
			ctx.workHandler().execute(() -> {
				Player player = ctx.player().orElseThrow();
				ServerLevel level = ((ServerLevel) ctx.level().orElseThrow());
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
