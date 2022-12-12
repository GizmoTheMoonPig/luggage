package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.entity.LuggageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LuggageRenderer extends MobRenderer<LuggageEntity, LuggageModel> {

	public LuggageRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new LuggageModel(ctx.bakeLayer(ClientEvents.LUGGAGE)), 0.5F);
		this.addLayer(new LuggagePowerLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(LuggageEntity luggage) {
		return new ResourceLocation(LuggageMod.ID, "textures/entity/luggage" +
				//maybe one day
				//(luggage.hasCustomName() && Objects.requireNonNull(luggage.getCustomName()).getString().equals("Chester") ? "_chester" : "") +
				(luggage.hasExtendedInventory() ? "_special" : "") + ".png");
	}

	public static class LuggagePowerLayer extends RenderLayer<LuggageEntity, LuggageModel> {

		public LuggagePowerLayer(RenderLayerParent<LuggageEntity, LuggageModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack ms, MultiBufferSource buffer, int light, LuggageEntity luggage, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
			if (luggage.hasExtendedInventory()) {
				assert Minecraft.getInstance().level != null;
				float f = (float) Minecraft.getInstance().level.getGameTime() + partialTicks;
				this.getParentModel().prepareMobModel(luggage, limbSwing, limbSwingAmount, partialTicks);
				VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.energySwirl(new ResourceLocation("textures/entity/creeper/creeper_armor.png"), f * 0.01F % 1.0F, f * 0.01F % 1.0F));
				this.getParentModel().setupAnim(luggage, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
				this.getParentModel().renderToBuffer(ms, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
			}
		}
	}
}
