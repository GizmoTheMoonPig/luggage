package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.client.LuggageScreen;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;


public record OpenLuggageScreenPacket(int containerID, int entityID) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation(LuggageMod.ID, "open_luggage_screen");

	public OpenLuggageScreenPacket(FriendlyByteBuf buf) {
		this(buf.readUnsignedByte(), buf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(this.containerID());
		buf.writeInt(this.entityID());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@SuppressWarnings("Convert2Lambda")
	public static void handle(OpenLuggageScreenPacket message, PlayPayloadContext ctx) {
		if (ctx.flow().isClientbound()) {
			ctx.workHandler().execute(new Runnable() {
				@Override
				public void run() {
					Level level = ctx.level().orElseThrow();
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
