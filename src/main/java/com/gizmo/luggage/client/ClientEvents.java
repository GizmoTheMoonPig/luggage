package com.gizmo.luggage.client;

import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageItem;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.CallLuggagePetsPacket;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.SitNearbyLuggagesPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class ClientEvents implements ClientModInitializer {

	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(Luggage.ID, "luggage"), "main");
	private static KeyMapping callKey;
	private static KeyMapping waitKey;

	public static void registerLayers() {
		EntityModelLayerRegistry.registerModelLayer(LUGGAGE, LuggageModel::create);
	}

	public static void registerEntityRenderer() {
		EntityRendererRegistry.register(Registries.EntityRegistry.LUGGAGE, LuggageRenderer::new);
	}

	public static void registerKeyBinding() {
		callKey = new KeyMapping(
				"keybind.luggage.call",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");

		waitKey = new KeyMapping(
				"keybind.luggage.wait",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_TAB,
				"key.categories.misc");
		KeyBindingHelper.registerKeyBinding(getCallKey());
		KeyBindingHelper.registerKeyBinding(getWaitKey());
	}

	public static KeyMapping getCallKey() {
		return callKey;
	}

	public static KeyMapping getWaitKey() {
		return waitKey;
	}

	@Override
	public void onInitializeClient() {
		registerEntityRenderer();
		registerKeyBinding();
		registerLayers();

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof LuggageItem.Tooltip tooltip)
				return new LuggageTooltipComponent(tooltip);
			return null;
		});
	}

	public static class ClientFabricEvents {

		public static void commandTheCreatures(int key, int scanCode, int action, int modifiers) {
			if (action != GLFW.GLFW_REPEAT && Minecraft.getInstance().player != null) {
				float pitch = Minecraft.getInstance().player.getRandom().nextFloat() * 0.1F + 0.9F;
				if (getCallKey().consumeClick()) {
					Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE_CALL, 1.0F, pitch);
					ClientPlayNetworking.send(CallLuggagePetsPacket.getID(), CallLuggagePetsPacket.encode(Minecraft.getInstance().player.getId()));
				} else if (getWaitKey().consumeClick()) {
					Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE_WAIT, 0.85F, pitch);
					ClientPlayNetworking.send(SitNearbyLuggagesPacket.getID(), SitNearbyLuggagesPacket.encode(Minecraft.getInstance().player.getId()));
				}
			}
		}
	}
}
