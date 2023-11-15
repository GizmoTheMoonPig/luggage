package com.gizmo.luggage.item;

import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.entity.EnderLuggage;
import net.minecraft.world.entity.EntityType;

public class EnderLuggageItem extends AbstractLuggageItem {
	public EnderLuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public EntityType<EnderLuggage> getLuggageEntity() {
		return LuggageRegistries.EntityRegistry.ENDER_LUGGAGE.get();
	}
}
