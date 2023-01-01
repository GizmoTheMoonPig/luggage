package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class Registries {

	public static class ItemRegistry {
		public static void register() {}

		public static final Item LUGGAGE = Registry.register(Registry.ITEM, new ResourceLocation(LuggageMod.ID, "luggage"), new LuggageItem(new FabricItemSettings().fireproof().maxCount(1).group(CreativeModeTab.TAB_TOOLS)));
	}

	public static class EntityRegistry {
		public static void register() {}

		public static final EntityType<LuggageEntity> LUGGAGE = Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(LuggageMod.ID, "luggage"), EntityType.Builder.of(LuggageEntity::new, MobCategory.MONSTER).fireImmune().sized(0.75F, 0.75F).build("luggage:luggage"));
	}

	public static class SoundRegistry {

		public static final SoundEvent LUGGAGE_KILLED = createEvent("entity.luggage.luggage.killed");
		public static final SoundEvent LUGGAGE_EAT_FOOD = createEvent("entity.luggage.luggage.eat_food");
		public static final SoundEvent LUGGAGE_EAT_ITEM = createEvent("entity.luggage.luggage.eat_item");
		public static final SoundEvent LUGGAGE_STEP = createEvent("entity.luggage.luggage.step");
		public static final SoundEvent WHISTLE_CALL = createEvent("entity.luggage.player.whistle_call");
		public static final SoundEvent WHISTLE_WAIT = createEvent("entity.luggage.player.whistle_wait");

		private static SoundEvent createEvent(String sound) {
			ResourceLocation name = new ResourceLocation(LuggageMod.ID, sound);
			return Registry.register(Registry.SOUND_EVENT, name, new SoundEvent(name));
		}

		static void registerSounds() {}
	}
}
