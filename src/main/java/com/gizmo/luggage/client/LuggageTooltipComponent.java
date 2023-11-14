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

	private static final int MARGIN_Y = 4;
	private static final int BORDER_WIDTH = 1;
	private static final int TEX_SIZE = 128;
	private static final int SLOT_SIZE_X = 18;
	private static final int SLOT_SIZE_Y = 18;
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
		return this.gridSizeY() * SLOT_SIZE_Y + 2 + MARGIN_Y;
	}

	//slots * slot width + padding
	@Override
	public int getWidth(Font font) {
		return this.gridSizeX() * SLOT_SIZE_X + 2;
	}


	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		int gridX = this.gridSizeX();
		int gridY = this.gridSizeY();
		int slot = 0;

		for (int yOffset = 0; yOffset < gridY; yOffset++) {
			for (int xOffset = 0; xOffset < gridX; xOffset++) {
				int slotX = x + xOffset * SLOT_SIZE_X + BORDER_WIDTH;
				int slotY = y + yOffset * SLOT_SIZE_Y + BORDER_WIDTH;
				this.renderSlot(slotX, slotY, slot++, font, graphics);
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
			graphics.renderItem(itemstack, x + BORDER_WIDTH, y + BORDER_WIDTH, slot);
			graphics.renderItemDecorations(font, itemstack, x + BORDER_WIDTH, y + BORDER_WIDTH);
		}
	}

	private void drawBorder(int startX, int startY, int endX, int endY, GuiGraphics graphics) {
		this.blit(graphics, startX, startY, Texture.BORDER_CORNER_TOP);
		this.blit(graphics, startX + endX * SLOT_SIZE_X + BORDER_WIDTH, startY, Texture.BORDER_CORNER_TOP);

		for (int j = 0; j < endX; ++j) {
			this.blit(graphics, startX + 1 + j * SLOT_SIZE_X, startY, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(graphics, startX + 1 + j * SLOT_SIZE_X, startY + endY * SLOT_SIZE_Y + 2, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int k = 0; k < endY; ++k) {
			this.blit(graphics, startX, startY + k * SLOT_SIZE_Y + BORDER_WIDTH, Texture.BORDER_VERTICAL);
			this.blit(graphics, startX + endX * SLOT_SIZE_X + BORDER_WIDTH, startY + k * SLOT_SIZE_Y + BORDER_WIDTH, Texture.BORDER_VERTICAL);
		}

		this.blit(graphics, startX, startY + endY * SLOT_SIZE_Y + 2, Texture.BORDER_CORNER_BOTTOM);
		this.blit(graphics, startX + endX * SLOT_SIZE_X + BORDER_WIDTH, startY + endY * SLOT_SIZE_Y + 2, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(GuiGraphics graphics, int x, int y, Texture texture) {
		graphics.blit(TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, TEX_SIZE, TEX_SIZE);
	}

	private int gridSizeX() {
		return 9;
	}

	private int gridSizeY() {
		return this.extended ? 6 : 3;
	}

	enum Texture {
		SLOT(0, 0, SLOT_SIZE_X, SLOT_SIZE_Y),
		BORDER_VERTICAL(0, SLOT_SIZE_X, 1, SLOT_SIZE_Y),
		BORDER_HORIZONTAL_TOP(0, SLOT_SIZE_Y, SLOT_SIZE_X, BORDER_WIDTH),
		BORDER_HORIZONTAL_BOTTOM(0, 60, SLOT_SIZE_X, BORDER_WIDTH),
		BORDER_CORNER_TOP(0, SLOT_SIZE_Y, BORDER_WIDTH, BORDER_WIDTH),
		BORDER_CORNER_BOTTOM(0, SLOT_SIZE_Y, BORDER_WIDTH, BORDER_WIDTH);

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