package com.gizmo.luggage.client;

import com.gizmo.luggage.Luggage;
import com.gizmo.luggage.entity.LuggageEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LuggageRenderer extends MobRenderer<LuggageEntity, LuggageModel> {

	public LuggageRenderer(EntityRendererManager ctx) {
		super(ctx, new LuggageModel(), 0.5F);
		this.addLayer(new LuggagePowerLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(LuggageEntity luggage) {
		return new ResourceLocation(Luggage.ID, "textures/entity/luggage" +
				//maybe one day
				//(luggage.hasCustomName() && Objects.requireNonNull(luggage.getCustomName()).getString().equals("Chester") ? "_chester" : "") +
				(luggage.hasExtendedInventory() ? "_special" : "") + ".png");
	}

	public static class LuggagePowerLayer extends LayerRenderer<LuggageEntity, LuggageModel> {

		public LuggagePowerLayer(IEntityRenderer<LuggageEntity, LuggageModel> parent) {
			super(parent);
		}

		@Override
		public void render(MatrixStack ms, IRenderTypeBuffer buffer, int light, LuggageEntity luggage, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
			if (luggage.hasExtendedInventory()) {
				assert Minecraft.getInstance().level != null;
				float f = (float) Minecraft.getInstance().level.getGameTime() + partialTicks;
				this.getParentModel().prepareMobModel(luggage, limbSwing, limbSwingAmount, partialTicks);
				IVertexBuilder vertexconsumer = buffer.getBuffer(RenderType.energySwirl(new ResourceLocation("textures/entity/creeper/creeper_armor.png"), f * 0.01F % 1.0F, f * 0.01F % 1.0F));
				this.getParentModel().setupAnim(luggage, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
				this.getParentModel().horns.visible = false;
				this.getParentModel().renderToBuffer(ms, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
			}
		}
	}
}
