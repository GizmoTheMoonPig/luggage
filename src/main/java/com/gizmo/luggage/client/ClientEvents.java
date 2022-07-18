package com.gizmo.luggage.client;


import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageItem;
import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.CallLuggagePetsPacket;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(Luggage.ID, "luggage"), "main");
	private static KeyMapping whistleKey;

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(LUGGAGE, LuggageModel::create);
	}

	@SubscribeEvent
	public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(Registries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
	}

	@SubscribeEvent
	public static void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(LuggageItem.Tooltip.class, LuggageTooltipComponent::new);
	}

	@SubscribeEvent
	public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
		whistleKey = new KeyMapping(
				"keybind.luggage.whistle",
				KeyConflictContext.IN_GAME,
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");
		event.register(getWhistleKey());
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

	@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientForgeEvents {

		@SubscribeEvent
		public static void callTheCreatures(InputEvent.Key event) {
			if (getWhistleKey().consumeClick() && event.getAction() != GLFW.GLFW_REPEAT && Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE.get(), 1.0F, 1.0F);
				LuggageNetworkHandler.CHANNEL.sendToServer(new CallLuggagePetsPacket(Minecraft.getInstance().player.getId()));
			}
		}
	}
}
