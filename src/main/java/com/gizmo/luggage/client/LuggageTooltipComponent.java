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
	private static final int BORDER_WIDTH = 2;
	private static final int SLOT_SIZE_X = 18;
	private static final int SLOT_SIZE_Y = 18;

	private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("container/bundle/background");
	private static final ResourceLocation SLOT_SPRITE = new ResourceLocation("container/bundle/slot");
	private final NonNullList<ItemStack> items;
	private final boolean extended;

	public LuggageTooltipComponent(LuggageItem.Tooltip tooltip) {
		this.items = tooltip.stacks();
		this.extended = tooltip.stack().getTag() != null && tooltip.stack().getTag().getBoolean(Luggage.EXTENDED_TAG);
	}

	@Override
	public int getHeight() {
		return this.backgroundHeight() + MARGIN_Y;
	}

	@Override
	public int getWidth(Font font) {
		return this.backgroundWidth();
	}

	private int backgroundWidth() {
		return this.gridSizeX() * SLOT_SIZE_X + BORDER_WIDTH;
	}

	private int backgroundHeight() {
		return this.gridSizeY() * SLOT_SIZE_Y + MARGIN_Y;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		int gridX = this.gridSizeX();
		int gridY = this.gridSizeY();
		graphics.blitSprite(BACKGROUND_SPRITE, x, y, this.backgroundWidth(), this.backgroundHeight());
		int slot = 0;

		for (int yOffset = 0; yOffset < gridY; yOffset++) {
			for (int xOffset = 0; xOffset < gridX; xOffset++) {
				int slotX = x + xOffset * SLOT_SIZE_X + 1;
				int slotY = y + yOffset * SLOT_SIZE_Y + 1;
				this.renderSlot(slotX, slotY, slot++, graphics, font);
			}
		}
	}

	private void renderSlot(int x, int y, int slot, GuiGraphics graphics, Font font) {
		ItemStack itemstack = this.items.get(slot);
		graphics.blitSprite(SLOT_SPRITE, x, y, 0, SLOT_SIZE_X, SLOT_SIZE_Y + 2);
		graphics.renderItem(itemstack, x + 1, y + 1, slot);
		graphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
	}

	private int gridSizeX() {
		return 9;
	}

	private int gridSizeY() {
		return this.extended ? 6 : 3;
	}
}