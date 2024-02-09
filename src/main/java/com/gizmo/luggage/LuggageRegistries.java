package com.gizmo.luggage;

import com.gizmo.luggage.entity.EnderLuggage;
import com.gizmo.luggage.entity.Luggage;
import com.gizmo.luggage.item.EnderLuggageItem;
import com.gizmo.luggage.item.LuggageItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LuggageRegistries {

	public static class ItemRegistry {
		public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LuggageMod.ID);

		public static final DeferredHolder<Item, Item> LUGGAGE = ITEMS.register("luggage", () -> new LuggageItem(new Item.Properties().fireResistant().stacksTo(1)));
		public static final DeferredHolder<Item, Item> ENDER_LUGGAGE = ITEMS.register("ender_luggage", () -> new EnderLuggageItem(new Item.Properties().fireResistant().stacksTo(1)));
	}

	public static class EntityRegistry {
		public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, LuggageMod.ID);

		public static final DeferredHolder<EntityType<?>, EntityType<Luggage>> LUGGAGE = ENTITIES.register("luggage", () -> EntityType.Builder.of(Luggage::new, MobCategory.CREATURE).fireImmune().sized(0.75F, 0.75F).build("luggage:luggage"));
		public static final DeferredHolder<EntityType<?>, EntityType<EnderLuggage>> ENDER_LUGGAGE = ENTITIES.register("ender_luggage", () -> EntityType.Builder.of(EnderLuggage::new, MobCategory.CREATURE).fireImmune().sized(0.75F, 0.75F).build("luggage:ender_luggage"));
	}

	public static class SoundRegistry {

		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, LuggageMod.ID);

		public static final DeferredHolder<SoundEvent, SoundEvent> LUGGAGE_KILLED = createEvent("entity.luggage.luggage.killed");
		public static final DeferredHolder<SoundEvent, SoundEvent> LUGGAGE_EAT_FOOD = createEvent("entity.luggage.luggage.eat_food");
		public static final DeferredHolder<SoundEvent, SoundEvent> LUGGAGE_EAT_ITEM = createEvent("entity.luggage.luggage.eat_item");
		public static final DeferredHolder<SoundEvent, SoundEvent> LUGGAGE_STEP = createEvent("entity.luggage.luggage.step");
		public static final DeferredHolder<SoundEvent, SoundEvent> WHISTLE_CALL = createEvent("entity.luggage.player.whistle_call");
		public static final DeferredHolder<SoundEvent, SoundEvent> WHISTLE_WAIT = createEvent("entity.luggage.player.whistle_wait");

		private static DeferredHolder<SoundEvent, SoundEvent> createEvent(String sound) {
			ResourceLocation name = new ResourceLocation(LuggageMod.ID, sound);
			return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(name));
		}
	}
}
