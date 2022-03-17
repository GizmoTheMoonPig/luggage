package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registries {

	public static class ItemRegistry {
		public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Luggage.ID);

		public static final RegistryObject<Item> LUGGAGE = ITEMS.register("luggage", () -> new LuggageItem(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS)));
	}

	public static class EntityRegistry {
		public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Luggage.ID);

		public static final RegistryObject<EntityType<LuggageEntity>> LUGGAGE = ENTITIES.register("luggage", () -> EntityType.Builder.of(LuggageEntity::new, MobCategory.MONSTER).fireImmune().sized(0.75F, 0.75F).build("luggage:luggage"));
	}

	public static class SoundRegistry {
		public static final SoundEvent LUGGAGE_EAT_FOOD = createEvent("entity.luggage.luggage.eat_food");
		public static final SoundEvent LUGGAGE_EAT_ITEM = createEvent("entity.luggage.luggage.eat_item");
		public static final SoundEvent LUGGAGE_STEP = createEvent("entity.luggage.luggage.step");

		private static SoundEvent createEvent(String sound) {
			ResourceLocation name = new ResourceLocation(Luggage.ID, sound);
			return new SoundEvent(name).setRegistryName(name);
		}

		static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
			event.getRegistry().registerAll(LUGGAGE_EAT_FOOD, LUGGAGE_EAT_ITEM, LUGGAGE_STEP);
		}
	}
}
