package com.gizmo.luggage.client;

import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.EnderLuggage;
import com.gizmo.luggage.entity.Luggage;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LuggageItemRenderer extends BlockEntityWithoutLevelRenderer {

	public LuggageItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		if (stack.is(Registries.ItemRegistry.LUGGAGE.get())) {
			assert Minecraft.getInstance().level != null;
			Luggage entity = Registries.EntityRegistry.LUGGAGE.get().create(Minecraft.getInstance().level);
			if (entity != null) {
				if (stack.getTag() != null) {
					entity.setExtendedInventory(stack.getTag().getBoolean(Luggage.EXTENDED_TAG));
				}
				this.renderEntity(entity, context, ms, light);
			}
		} else if (stack.is(Registries.ItemRegistry.ENDER_LUGGAGE.get())) {
			assert Minecraft.getInstance().level != null;
			EnderLuggage entity = Registries.EntityRegistry.ENDER_LUGGAGE.get().create(Minecraft.getInstance().level);
			if (entity != null) {
				this.renderEntity(entity, context, ms, light);
			}
		}
	}

	private void renderEntity(Entity entity, ItemDisplayContext context, PoseStack stack, int light) {
		float partialTicks = Minecraft.getInstance().getFrameTime();
		float partialTicksForRender = Minecraft.getInstance().isPaused() ? 0 : partialTicks;
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.translate(-0.5F, -0.2F, 0.0F);
		stack.scale(0.8F, 0.8F, 0.8F);
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		boolean hitboxes = dispatcher.shouldRenderHitBoxes();
		dispatcher.setRenderShadow(false);
		dispatcher.setRenderHitBoxes(false);
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() ->
				dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicksForRender, stack, source, context == ItemDisplayContext.GUI ? 15728880 : light));
		source.endBatch();
		dispatcher.setRenderShadow(true);
		dispatcher.setRenderHitBoxes(hitboxes);
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}
}
