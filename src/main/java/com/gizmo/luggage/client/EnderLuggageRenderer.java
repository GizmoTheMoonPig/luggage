package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.EnderLuggage;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EnderLuggageRenderer extends MobRenderer<EnderLuggage, LuggageModel<EnderLuggage>> {
	public EnderLuggageRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new LuggageModel<>(ctx.bakeLayer(ClientEvents.LUGGAGE)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(EnderLuggage luggage) {
		return new ResourceLocation(LuggageMod.ID, "textures/entity/ender_luggage.png");
	}
}
