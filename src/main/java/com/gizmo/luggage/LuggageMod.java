package com.gizmo.luggage;

import com.gizmo.luggage.client.ClientEvents;
import com.gizmo.luggage.entity.EnderLuggage;
import com.gizmo.luggage.entity.Luggage;
import com.gizmo.luggage.network.CallLuggagePacket;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import com.gizmo.luggage.network.SitNearbyLuggagesPacket;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod(LuggageMod.ID)
public class LuggageMod {
	public static final String ID = "luggage";

	public LuggageMod(IEventBus bus, Dist dist) {
		LuggageRegistries.ItemRegistry.ITEMS.register(bus);
		LuggageRegistries.SoundRegistry.SOUNDS.register(bus);
		LuggageRegistries.EntityRegistry.ENTITIES.register(bus);

		if (dist.isClient()) {
			ClientEvents.init(bus);
		}

		bus.addListener(this::addToTab);
		bus.addListener(this::setupPackets);
		bus.addListener(this::addAttributes);
		bus.addListener(RegisterCapabilitiesEvent.class, event -> event.registerEntity(Capabilities.ItemHandler.ENTITY, LuggageRegistries.EntityRegistry.LUGGAGE.get(), (entity, ctx) -> new InvWrapper(entity.getInventory())));
		NeoForge.EVENT_BUS.addListener(this::neverKillLuggage);
	}

	public void setupPackets(RegisterPayloadHandlerEvent event) {
		IPayloadRegistrar registrar = event.registrar(ID).versioned("1.0.0").optional();
		registrar.play(CallLuggagePacket.ID, (buf) -> new CallLuggagePacket(), payload -> payload.server((message, ctx) -> CallLuggagePacket.handle(ctx)));
		registrar.play(OpenLuggageScreenPacket.ID, OpenLuggageScreenPacket::new, payload -> payload.client(OpenLuggageScreenPacket::handle));
		registrar.play(SitNearbyLuggagesPacket.ID, (buf) -> new SitNearbyLuggagesPacket(), payload -> payload.server((message, ctx) -> SitNearbyLuggagesPacket.handle(ctx)));
	}

	public void addAttributes(EntityAttributeCreationEvent event) {
		event.put(LuggageRegistries.EntityRegistry.LUGGAGE.get(), Luggage.registerAttributes().build());
		event.put(LuggageRegistries.EntityRegistry.ENDER_LUGGAGE.get(), EnderLuggage.registerAttributes().build());
	}

	public void addToTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			//normal luggage
			event.accept(LuggageRegistries.ItemRegistry.LUGGAGE.get());
			//add charged luggage too
			ItemStack item = new ItemStack(LuggageRegistries.ItemRegistry.LUGGAGE.get());
			CompoundTag tag = new CompoundTag();
			tag.putBoolean(Luggage.EXTENDED_TAG, true);
			item.setTag(tag);
			event.accept(item);
			event.accept(LuggageRegistries.ItemRegistry.ENDER_LUGGAGE.get());
		}
	}

	public void neverKillLuggage(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ItemEntity item && item.getItem().is(LuggageRegistries.ItemRegistry.LUGGAGE.get()) &&
				item.getItem().getTag() != null && item.getItem().getTag().contains(Luggage.INVENTORY_TAG)) {
			item.setInvulnerable(true);
			item.setUnlimitedLifetime();
		}
	}
}
