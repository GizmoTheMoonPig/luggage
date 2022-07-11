package com.gizmo.luggage.client;


import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageItem;
import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.CallLuggagePetsPacket;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class ClientEvents implements ClientModInitializer {

	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(Luggage.ID, "luggage"), "main");

	private static KeyMapping whistleKey;

	public static void registerLayers() {
		EntityModelLayerRegistry.registerModelLayer(LUGGAGE, LuggageModel::create);
	}

	public static void registerEntityRenderer() {
		EntityRendererRegistry.register(Registries.EntityRegistry.LUGGAGE, LuggageRenderer::new);
	}

	@Override
	public void onInitializeClient() {
		whistleKey = new KeyMapping(
				"keybind.luggage.whistle",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");
		KeyBindingHelper.registerKeyBinding(getWhistleKey());

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof LuggageItem.Tooltip tooltip)
				return new LuggageTooltipComponent(tooltip);
			return null;
		});

		registerLayers();
		registerEntityRenderer();

		ClientTickEvents.END_CLIENT_TICK.register(ClientFabricEvents::callTheCreatures);
	}

	//I hate this with a burning passion
	//however, calling this in the main packet attempts to load the screen class on the server for some reason???
	//I have no fucking clue, but this works so idc
	public static void handlePacket(OpenLuggageScreenPacket message) {
		Entity entity = Minecraft.getInstance().level.getEntity(message.getEntityId());
		if (entity instanceof LuggageEntity luggage) {
			LocalPlayer localplayer = Minecraft.getInstance().player;
			SimpleContainer simplecontainer = new SimpleContainer(luggage.hasExtendedInventory() ? 54 : 27);
			LuggageMenu menu = new LuggageMenu(message.getContainerId(), localplayer.getInventory(), simplecontainer, luggage);
			localplayer.containerMenu = menu;
			Minecraft.getInstance().setScreen(new LuggageScreen(menu, localplayer.getInventory(), luggage));
		}
	}

	public static KeyMapping getWhistleKey() {
		return whistleKey;
	}

	public static class ClientFabricEvents {

		public static void callTheCreatures(Minecraft client) {
			if (getWhistleKey().consumeClick() && Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE.get(), 1.0F, 1.0F);
				ClientPlayNetworking.send(CallLuggagePetsPacket.getID(), new CallLuggagePetsPacket(Minecraft.getInstance().player.getId()).encode());
			}
		}
	}
}
