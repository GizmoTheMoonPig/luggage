package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.entity.Luggage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LuggageScreen extends AbstractContainerScreen<LuggageMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
	private final int containerRows;

	public LuggageScreen(LuggageMenu menu, Inventory inventory, Luggage entity) {
		super(menu, inventory, entity.getDisplayName());
		this.passEvents = false;
		this.containerRows = entity.hasExtendedInventory() ? 6 : 3;
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	public void render(PoseStack ms, int x, int y, float partialTicks) {
		this.renderBackground(ms);
		super.render(ms, x, y, partialTicks);
		this.renderTooltip(ms, x, y);
	}

	protected void renderBg(PoseStack ms, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		blit(ms, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
		blit(ms, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
	}
}
