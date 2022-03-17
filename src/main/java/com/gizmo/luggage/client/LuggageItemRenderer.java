package com.gizmo.luggage.client;

import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.Registries;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;

public class LuggageItemRenderer extends BlockEntityWithoutLevelRenderer {

	public LuggageItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		if(stack.is(Registries.ItemRegistry.LUGGAGE.get())) {
			assert Minecraft.getInstance().level != null;
			LuggageEntity entity = Registries.EntityRegistry.LUGGAGE.get().create(Minecraft.getInstance().level);
			if (entity != null) {
				entity.setExtendedInventory(stack.getOrCreateTag().getBoolean("Extended"));
				float partialTicks = Minecraft.getInstance().getFrameTime();
				Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
				Quaternion quaternion1 = Vector3f.XP.rotationDegrees(20.0F);
				float partialTicksForRender = Minecraft.getInstance().isPaused() ? 0 : partialTicks;

				ms.scale(-1.0F, -1.0F, 1.0F);
				ms.translate(-0.5F, -0.2F, 0.0F);
				ms.scale(0.8F, 0.8F, 0.8F);
				ms.mulPose(quaternion);
				if(type == ItemTransforms.TransformType.GUI) {
					ms.mulPose(Vector3f.XP.rotationDegrees(20));
					ms.mulPose(Vector3f.YP.rotationDegrees(45));
					ms.mulPose(Vector3f.ZP.rotationDegrees(0));
				}
				EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
				quaternion1.conj();
				entityrenderdispatcher.overrideCameraOrientation(quaternion1);
				entityrenderdispatcher.setRenderShadow(false);
				MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
				RenderSystem.runAsFancy(() ->
						entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicksForRender, ms, source, type == ItemTransforms.TransformType.GUI ? 15728880 : light));
				source.endBatch();
				entityrenderdispatcher.setRenderShadow(true);
				entity.setYRot(0.0F);
				entity.setXRot(0.0F);
				entity.yBodyRot = 0.0F;
				entity.yHeadRotO = 0.0F;
				entity.yHeadRot = 0.0F;
				RenderSystem.applyModelViewMatrix();
				Lighting.setupFor3DItems();
			}
		}
	}
}
