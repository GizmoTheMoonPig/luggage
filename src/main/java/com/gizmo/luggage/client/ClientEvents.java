package com.gizmo.luggage.client;


import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.CallLuggagePetsPacket;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	private static KeyBinding whistleKey;

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(Registries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
		whistleKey = new KeyBinding(
				"keybind.luggage.whistle",
				KeyConflictContext.IN_GAME,
				InputMappings.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");
		ClientRegistry.registerKeyBinding(getWhistleKey());
	}

	//I hate this with a burning passion
	//however, calling this in the main packet attempts to load the screen class on the server for some reason???
	//I have no fucking clue, but this works so idc
	public static void handlePacket(OpenLuggageScreenPacket message) {
		Entity entity = Minecraft.getInstance().level.getEntity(message.getEntityId());
		if (entity instanceof LuggageEntity) {
			LuggageEntity luggage = (LuggageEntity) entity;
			ClientPlayerEntity localplayer = Minecraft.getInstance().player;
			Inventory simplecontainer = new Inventory(luggage.hasExtendedInventory() ? 54 : 27);
			LuggageMenu menu = new LuggageMenu(message.getContainerId(), localplayer.inventory, simplecontainer, luggage);
			localplayer.containerMenu = menu;
			Minecraft.getInstance().setScreen(new LuggageScreen(menu, localplayer.inventory, luggage));
		}
	}

	public static KeyBinding getWhistleKey() {
		return whistleKey;
	}


	@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientForgeEvents {

		@SubscribeEvent
		public static void callTheCreatures(InputEvent.KeyInputEvent event) {
			if (getWhistleKey().consumeClick() && Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE, 1.0F, 1.0F);
				LuggageNetworkHandler.CHANNEL.sendToServer(new CallLuggagePetsPacket(Minecraft.getInstance().player.getId()));
			}
		}
	}
}
