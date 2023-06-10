package com.gizmo.luggage;

import com.gizmo.luggage.entity.EnderLuggage;
import com.gizmo.luggage.entity.Luggage;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LuggageMod.ID)
@Mod.EventBusSubscriber(modid = LuggageMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LuggageMod {
	public static final String ID = "luggage";

	public LuggageMod() {
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		modbus.addListener(this::setup);
		LuggageRegistries.EntityRegistry.ENTITIES.register(modbus);
		LuggageRegistries.ItemRegistry.ITEMS.register(modbus);
		LuggageRegistries.SoundRegistry.SOUNDS.register(modbus);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(FMLCommonSetupEvent event) {
		LuggageNetworkHandler.init();
	}

	@SubscribeEvent
	public static void addAttributes(EntityAttributeCreationEvent event) {
		event.put(LuggageRegistries.EntityRegistry.LUGGAGE.get(), Luggage.registerAttributes().build());
		event.put(LuggageRegistries.EntityRegistry.ENDER_LUGGAGE.get(), EnderLuggage.registerAttributes().build());
	}

	@SubscribeEvent
	public static void addToTab(BuildCreativeModeTabContentsEvent event) {
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

	@Mod.EventBusSubscriber(modid = LuggageMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEvents {
		@SubscribeEvent
		public static void neverKillLuggage(EntityJoinLevelEvent event) {
			if (event.getEntity() instanceof ItemEntity item && item.getItem().is(LuggageRegistries.ItemRegistry.LUGGAGE.get()) &&
					item.getItem().getTag() != null && item.getItem().getTag().contains(Luggage.INVENTORY_TAG)) {
				item.setInvulnerable(true);
				item.setUnlimitedLifetime();
			}
		}
	}
}
