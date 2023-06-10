package com.gizmo.luggage.client;

import com.gizmo.luggage.entity.Luggage;
import com.gizmo.luggage.item.LuggageItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

//Modified version of ClientBundleTooltip
public class LuggageTooltipComponent implements ClientTooltipComponent {

	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
	private final NonNullList<ItemStack> items;
	private final boolean extended;

	public LuggageTooltipComponent(LuggageItem.Tooltip tooltip) {
		this.items = tooltip.stacks();
		this.extended = tooltip.stack().getTag() != null && tooltip.stack().getTag().getBoolean(Luggage.EXTENDED_TAG);
	}

	//slots * slot height + padding
	@Override
	public int getHeight() {
		return (this.extended ? 6 : 3) * 18 + 6;
	}

	//slots * slot width + padding
	@Override
	public int getWidth(Font font) {
		return 9 * 18 + 2;
	}


	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		int gridX = this.gridSizeX();
		int gridY = this.gridSizeY();
		int slot = 0;

		for (int l = 0; l < gridY; ++l) {
			for (int i1 = 0; i1 < gridX; ++i1) {
				int j1 = x + i1 * 18 + 1;
				int k1 = y + l * 18 + 1;
				this.renderSlot(j1, k1, slot++, font, graphics);
			}
		}

		this.drawBorder(x, y, gridX, gridY, graphics);
	}

	private void renderSlot(int x, int y, int slot, Font font, GuiGraphics graphics) {
		if (slot >= this.items.size()) {
			this.blit(graphics, x, y, Texture.SLOT);
		} else {
			ItemStack itemstack = this.items.get(slot);
			this.blit(graphics, x, y, Texture.SLOT);
			graphics.renderItem(itemstack, x + 1, y + 1, slot);
			graphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
			if (slot == 0) {
				AbstractContainerScreen.renderSlotHighlight(graphics, x + 1, y + 1, 0);
			}
		}
	}

	private void drawBorder(int startX, int startY, int endX, int endY, GuiGraphics graphics) {
		this.blit(graphics, startX, startY, Texture.BORDER_CORNER_TOP);
		this.blit(graphics, startX + endX * 18 + 1, startY, Texture.BORDER_CORNER_TOP);

		for (int j = 0; j < endX; ++j) {
			this.blit(graphics, startX + 1 + j * 18, startY, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(graphics, startX + 1 + j * 18, startY + endY * 19 - 1, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int k = 0; k < endY; ++k) {
			this.blit(graphics, startX, startY + k * 18 + 1, Texture.BORDER_VERTICAL);
			this.blit(graphics, startX + endX * 18 + 1, startY + k * 18 + 1, Texture.BORDER_VERTICAL);
		}

		this.blit(graphics, startX, startY + endY * 19 - 1, Texture.BORDER_CORNER_BOTTOM);
		this.blit(graphics, startX + endX * 18 + 1, startY + endY * 18 + 2, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(GuiGraphics graphics, int x, int y, Texture texture) {
		graphics.blit(TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
	}

	private int gridSizeX() {
		return 9;
	}

	private int gridSizeY() {
		return this.extended ? 6 : 3;
	}

	enum Texture {
		SLOT(0, 0, 18, 20),
		BORDER_VERTICAL(0, 18, 1, 20),
		BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
		BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
		BORDER_CORNER_TOP(0, 20, 1, 1),
		BORDER_CORNER_BOTTOM(0, 60, 1, 1);

		public final int x;
		public final int y;
		public final int w;
		public final int h;

		Texture(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
}