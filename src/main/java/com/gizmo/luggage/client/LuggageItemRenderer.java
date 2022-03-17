package com.gizmo.luggage.client;

import com.gizmo.luggage.entity.LuggageEntity;
import com.gizmo.luggage.Registries;
import net.minecraft.client.renderer.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.item.ItemStack;

public class LuggageItemRenderer extends ItemStackTileEntityRenderer {

	public LuggageItemRenderer() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType type, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		if(stack.getItem() == Registries.ItemRegistry.LUGGAGE.get()) {
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
				if(type == ItemCameraTransforms.TransformType.GUI) {
					ms.mulPose(Vector3f.XP.rotationDegrees(20));
					ms.mulPose(Vector3f.YP.rotationDegrees(45));
					ms.mulPose(Vector3f.ZP.rotationDegrees(0));
				}
				EntityRendererManager entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
				quaternion1.conj();
				entityrenderdispatcher.overrideCameraOrientation(quaternion1);
				entityrenderdispatcher.setRenderShadow(false);
				IRenderTypeBuffer.Impl source = Minecraft.getInstance().renderBuffers().bufferSource();
				RenderSystem.runAsFancy(() ->
						entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicksForRender, ms, source, type == ItemCameraTransforms.TransformType.GUI ? 15728880 : light));
				source.endBatch();
				entityrenderdispatcher.setRenderShadow(true);
				entity.yRot = 0.0F;
				entity.xRot = 0.0F;
				entity.yBodyRot = 0.0F;
				entity.yHeadRotO = 0.0F;
				entity.yHeadRot = 0.0F;
				//RenderSystem.applyModelViewMatrix();
				RenderHelper.setupFor3DItems();
			}
		}
	}
}
