package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Luggage.ID)
@Mod.EventBusSubscriber(modid = Luggage.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Luggage {
    public static final String ID = "luggage";

	public Luggage() {
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		modbus.addListener(this::setup);
		Registries.EntityRegistry.ENTITIES.register(modbus);
		Registries.ItemRegistry.ITEMS.register(modbus);
		modbus.addGenericListener(SoundEvent.class, Registries.SoundRegistry::registerSounds);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(FMLCommonSetupEvent event) {
		LuggageNetworkHandler.init();
	}

	@SubscribeEvent
	public static void addAttributes(EntityAttributeCreationEvent event) {
		event.put(Registries.EntityRegistry.LUGGAGE.get(), LuggageEntity.registerAttributes().build());
	}

	@Mod.EventBusSubscriber(modid = Luggage.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEvents {
		@SubscribeEvent
		public static void neverKillLuggage(EntityJoinWorldEvent event) {
			if(event.getEntity() instanceof ItemEntity item && item.getItem().is(Registries.ItemRegistry.LUGGAGE.get()) && item.getItem().getOrCreateTag().contains("Inventory")) {
				item.setInvulnerable(true);
				item.setUnlimitedLifetime();
			}
		}
	}
}
