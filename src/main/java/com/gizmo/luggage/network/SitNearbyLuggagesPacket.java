package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.AbstractLuggage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class SitNearbyLuggagesPacket implements CustomPacketPayload {

	public static final Type<SitNearbyLuggagesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LuggageMod.ID, "sit_nearby_luggage"));
	public static final SitNearbyLuggagesPacket INSTANCE = new SitNearbyLuggagesPacket();
	public static final StreamCodec<ByteBuf, SitNearbyLuggagesPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			List<AbstractLuggage> nearbyOwnedLuggages = player.level().getEntitiesOfClass(AbstractLuggage.class, player.getBoundingBox().inflate(8.0F), entity -> entity.getOwner() == player);
			if (!nearbyOwnedLuggages.isEmpty()) {
				for (AbstractLuggage luggage : nearbyOwnedLuggages) {
					luggage.setInSittingPose(!luggage.isInSittingPose());
				}
			}
		});
	}
}
