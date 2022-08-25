package com.gizmo.luggage.network;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.client.LuggageScreen;
import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
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

		@SuppressWarnings("Convert2Lambda")
		public static boolean onMessage(OpenLuggageScreenPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(new Runnable() {
				@Override
				public void run() {
					assert Minecraft.getInstance().level != null;
					Entity entity = Minecraft.getInstance().level.getEntity(message.getEntityId());
					if (entity instanceof LuggageEntity luggage) {
						LocalPlayer localplayer = Minecraft.getInstance().player;
						SimpleContainer simplecontainer = new SimpleContainer(luggage.hasExtendedInventory() ? 54 : 27);
						assert localplayer != null;
						LuggageMenu menu = new LuggageMenu(message.getContainerId(), localplayer.getInventory(), simplecontainer, luggage);
						localplayer.containerMenu = menu;
						Minecraft.getInstance().setScreen(new LuggageScreen(menu, localplayer.getInventory(), luggage));
					}
				}
			});
			ctx.get().setPacketHandled(true);
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
