package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.client.LuggageScreen;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenLuggageScreenPacket(int containerID, int entityID) implements CustomPacketPayload {

	public static final Type<OpenLuggageScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LuggageMod.ID, "open_luggage_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenLuggageScreenPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, OpenLuggageScreenPacket::containerID,
			ByteBufCodecs.INT, OpenLuggageScreenPacket::entityID,
			OpenLuggageScreenPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@SuppressWarnings("Convert2Lambda")
	public static void handle(OpenLuggageScreenPacket message, IPayloadContext ctx) {
		if (ctx.flow().isClientbound()) {
			ctx.enqueueWork(new Runnable() {
				@Override
				public void run() {
					Level level = ctx.player().level();
					Entity entity = level.getEntity(message.entityID());
					if (entity instanceof Luggage luggage) {
						LocalPlayer localplayer = Minecraft.getInstance().player;
						SimpleContainer simplecontainer = new SimpleContainer(luggage.hasExtendedInventory() ? 54 : 27);
						assert localplayer != null;
						LuggageMenu menu = new LuggageMenu(message.containerID(), localplayer.getInventory(), simplecontainer, luggage);
						localplayer.containerMenu = menu;
						Minecraft.getInstance().setScreen(new LuggageScreen(menu, localplayer.getInventory(), luggage));
					}
				}
			});
		}
	}
}
