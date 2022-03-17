package com.gizmo.luggage.client;


import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(Registries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
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
}
