package com.gizmo.luggage;

import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registries {

	public static class ItemRegistry {
		public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Luggage.ID);

		public static final RegistryObject<Item> LUGGAGE = ITEMS.register("luggage", () -> new LuggageItem(new Item.Properties().fireResistant().stacksTo(1).tab(ItemGroup.TAB_TOOLS).setISTER(() -> LuggageItemRenderer::new)));
	}

	public static class EntityRegistry {
		public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Luggage.ID);

		public static final RegistryObject<EntityType<LuggageEntity>> LUGGAGE = ENTITIES.register("luggage", () -> EntityType.Builder.of(LuggageEntity::new, EntityClassification.MONSTER).fireImmune().sized(0.75F, 0.75F).build("luggage:luggage"));
	}

	public static class SoundRegistry {
		public static final SoundEvent LUGGAGE_EAT_FOOD = createEvent("entity.luggage.luggage.eat_food");
		public static final SoundEvent LUGGAGE_EAT_ITEM = createEvent("entity.luggage.luggage.eat_item");
		public static final SoundEvent LUGGAGE_STEP = createEvent("entity.luggage.luggage.step");
		public static final SoundEvent WHISTLE = createEvent("entity.luggage.player.whistle");

		private static SoundEvent createEvent(String sound) {
			ResourceLocation name = new ResourceLocation(Luggage.ID, sound);
			return new SoundEvent(name).setRegistryName(name);
		}

		static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
			event.getRegistry().registerAll(LUGGAGE_EAT_FOOD, LUGGAGE_EAT_ITEM, LUGGAGE_STEP, WHISTLE);
		}
	}
}
