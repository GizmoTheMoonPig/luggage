package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.AbstractLuggage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

public class SitNearbyLuggagesPacket implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation(LuggageMod.ID, "sit_luggage");

	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static void handle(PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Player player = ctx.player().orElseThrow();
			List<AbstractLuggage> nearbyOwnedLuggages = player.level().getEntitiesOfClass(AbstractLuggage.class, player.getBoundingBox().inflate(8.0F), entity -> entity.getOwner() == player);
			if (!nearbyOwnedLuggages.isEmpty()) {
				for (AbstractLuggage luggage : nearbyOwnedLuggages) {
					luggage.setInSittingPose(!luggage.isInSittingPose());
				}
			}
		});
	}

}
