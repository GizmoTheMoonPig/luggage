package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LuggageScreen extends AbstractContainerScreen<LuggageMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
	private final int containerRows;

	public LuggageScreen(LuggageMenu menu, Inventory inventory, Luggage entity) {
		super(menu, inventory, entity.getDisplayName());
		this.containerRows = entity.hasExtendedInventory() ? 6 : 3;
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
		this.renderBackground(graphics, x, y, partialTicks);
		super.render(graphics, x, y, partialTicks);
		this.renderTooltip(graphics, x, y);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
		graphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
	}
}
