package com.gizmo.luggage.client;


import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Luggage.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(Luggage.ID, "luggage"), "main");

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(LUGGAGE, LuggageModel::create);
	}

	@SubscribeEvent
	public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(Registries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
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
}
