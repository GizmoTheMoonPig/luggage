package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.client.LuggageScreen;
import com.gizmo.luggage.entity.LuggageEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;

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

	public FriendlyByteBuf encode() {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeByte(this.containerId);
		buf.writeInt(this.entityId);
		return buf;
	}

	public static ResourceLocation getID() {
		return new ResourceLocation(LuggageMod.ID, "open_luggage_screen_packet");
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static void onMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			OpenLuggageScreenPacket message = new OpenLuggageScreenPacket(buf);
			client.execute(new Runnable() {
				@Override
				public void run() {
					assert Minecraft.getInstance().level != null;
					Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
					if (entity instanceof LuggageEntity luggage) {
						LocalPlayer localplayer = Minecraft.getInstance().player;
						SimpleContainer simplecontainer = new SimpleContainer(luggage.hasExtendedInventory() ? 54 : 27);
						assert localplayer != null;
						LuggageMenu menu = new LuggageMenu(message.containerId, localplayer.getInventory(), simplecontainer, luggage);
						localplayer.containerMenu = menu;
						Minecraft.getInstance().setScreen(new LuggageScreen(menu, localplayer.getInventory(), luggage));
					}
				}
			});
			return true;
		}
	}
}
