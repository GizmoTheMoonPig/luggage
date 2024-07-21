package com.gizmo.luggage;

import com.gizmo.luggage.client.ClientEvents;
import com.gizmo.luggage.entity.EnderLuggage;
import com.gizmo.luggage.entity.Luggage;
import com.gizmo.luggage.network.CallLuggagePacket;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import com.gizmo.luggage.network.SitNearbyLuggagesPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(LuggageMod.ID)
public class LuggageMod {
	public static final String ID = "luggage";

	public LuggageMod(IEventBus bus, Dist dist) {
		LuggageRegistries.ITEMS.register(bus);
		LuggageRegistries.SOUNDS.register(bus);
		LuggageRegistries.ENTITIES.register(bus);
		LuggageRegistries.COMPONENTS.register(bus);

		if (dist.isClient()) {
			ClientEvents.init(bus);
		}

		bus.addListener(this::addToTab);
		bus.addListener(this::setupPackets);
		bus.addListener(this::addAttributes);
		bus.addListener(RegisterCapabilitiesEvent.class, event -> event.registerEntity(Capabilities.ItemHandler.ENTITY, LuggageRegistries.LUGGAGE.get(), (entity, ctx) -> new InvWrapper(entity.getInventory())));
		NeoForge.EVENT_BUS.addListener(this::neverKillLuggage);
	}

	public void setupPackets(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(ID).versioned("1.0.0").optional();
		registrar.playToServer(CallLuggagePacket.TYPE, CallLuggagePacket.STREAM_CODEC, (payload, context) -> CallLuggagePacket.handle(context));
		registrar.playToClient(OpenLuggageScreenPacket.TYPE, OpenLuggageScreenPacket.STREAM_CODEC, OpenLuggageScreenPacket::handle);
		registrar.playToServer(SitNearbyLuggagesPacket.TYPE, SitNearbyLuggagesPacket.STREAM_CODEC, (payload, context) -> SitNearbyLuggagesPacket.handle(context));
	}

	public void addAttributes(EntityAttributeCreationEvent event) {
		event.put(LuggageRegistries.LUGGAGE.get(), Luggage.registerAttributes().build());
		event.put(LuggageRegistries.ENDER_LUGGAGE.get(), EnderLuggage.registerAttributes().build());
	}

	public void addToTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			//normal luggage
			event.accept(LuggageRegistries.LUGGAGE_ITEM.get());
			//add charged luggage too
			ItemStack item = new ItemStack(LuggageRegistries.LUGGAGE_ITEM.get());
			item.set(LuggageRegistries.EXTENDED, Unit.INSTANCE);
			event.accept(item);
			event.accept(LuggageRegistries.ENDER_LUGGAGE_ITEM.get());
		}
	}

	public void neverKillLuggage(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ItemEntity item && item.getItem().is(LuggageRegistries.LUGGAGE_ITEM.get()) && item.getItem().has(DataComponents.CONTAINER)) {
			item.setInvulnerable(true);
			item.setUnlimitedLifetime();
		}
	}
}
