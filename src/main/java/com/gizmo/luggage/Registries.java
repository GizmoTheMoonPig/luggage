package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registries {

	public static class ItemRegistry {
		public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Luggage.ID);

		public static final RegistryObject<Item> LUGGAGE = ITEMS.register("luggage", () -> new LuggageItem(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS)));
	}

	public static class EntityRegistry {
		public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Luggage.ID);

		public static final RegistryObject<EntityType<LuggageEntity>> LUGGAGE = ENTITIES.register("luggage", () -> EntityType.Builder.of(LuggageEntity::new, MobCategory.CREATURE).fireImmune().sized(0.75F, 0.75F).build("luggage:luggage"));
	}

	public static class SoundRegistry {

		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Luggage.ID);

		public static final RegistryObject<SoundEvent> LUGGAGE_KILLED = createEvent("entity.luggage.luggage.killed");
		public static final RegistryObject<SoundEvent> LUGGAGE_EAT_FOOD = createEvent("entity.luggage.luggage.eat_food");
		public static final RegistryObject<SoundEvent> LUGGAGE_EAT_ITEM = createEvent("entity.luggage.luggage.eat_item");
		public static final RegistryObject<SoundEvent> LUGGAGE_STEP = createEvent("entity.luggage.luggage.step");
		public static final RegistryObject<SoundEvent> WHISTLE_CALL = createEvent("entity.luggage.player.whistle_call");
		public static final RegistryObject<SoundEvent> WHISTLE_WAIT = createEvent("entity.luggage.player.whistle_wait");

		private static RegistryObject<SoundEvent> createEvent(String sound) {
			ResourceLocation name = new ResourceLocation(Luggage.ID, sound);
			return SOUNDS.register(sound, () -> new SoundEvent(name));
		}
	}
}
