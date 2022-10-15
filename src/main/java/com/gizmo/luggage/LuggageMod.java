package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class LuggageMod implements ModInitializer {
	public static final String ID = "luggage";

	@Override
	public void onInitialize() {
		Registries.EntityRegistry.register();
		Registries.ItemRegistry.register();
		Registries.SoundRegistry.registerSounds();
		LuggageNetworkHandler.init();
		addAttributes();

		ServerEntityEvents.ENTITY_LOAD.register(FabricEvents::neverKillLuggage);
	}

	public static void addAttributes() {
		FabricDefaultAttributeRegistry.register(Registries.EntityRegistry.LUGGAGE, LuggageEntity.registerAttributes());
	}

	public static class FabricEvents {
		public static void neverKillLuggage(Entity entity, ServerLevel world) {
			if (entity instanceof ItemEntity item && item.getItem().is(Registries.ItemRegistry.LUGGAGE) &&
					item.getItem().getTag() != null && item.getItem().getTag().contains(LuggageEntity.INVENTORY_TAG)) {
				item.setInvulnerable(true);
				item.setUnlimitedLifetime();
			}
		}
	}
}
