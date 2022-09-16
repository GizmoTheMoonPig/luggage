package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
		this.extended = tooltip.stack().getTag() != null && tooltip.stack().getTag().getBoolean("Extended");
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
	public void renderImage(Font font, int x, int y, PoseStack stack, ItemRenderer renderer, int i) {
		int gridX = this.gridSizeX();
		int gridY = this.gridSizeY();
		int slot = 0;

		for (int l = 0; l < gridY; ++l) {
			for (int i1 = 0; i1 < gridX; ++i1) {
				int j1 = x + i1 * 18 + 1;
				int k1 = y + l * 18 + 1;
				this.renderSlot(j1, k1, slot++, font, stack, renderer, i);
			}
		}

		this.drawBorder(x, y, gridX, gridY, stack, i);
	}

	private void renderSlot(int x, int y, int slot, Font font, PoseStack stack, ItemRenderer renderer, int i) {
		if (slot >= this.items.size()) {
			this.blit(stack, x, y, i, Texture.SLOT);
		} else {
			ItemStack itemstack = this.items.get(slot);
			this.blit(stack, x, y, i, Texture.SLOT);
			renderer.renderAndDecorateItem(itemstack, x + 1, y + 1, slot);
			renderer.renderGuiItemDecorations(font, itemstack, x + 1, y + 1);
			if (slot == 0) {
				AbstractContainerScreen.renderSlotHighlight(stack, x + 1, y + 1, i);
			}
		}
	}

	private void drawBorder(int startX, int startY, int endX, int endY, PoseStack stack, int i) {
		this.blit(stack, startX, startY, i, Texture.BORDER_CORNER_TOP);
		this.blit(stack, startX + endX * 18 + 1, startY, i, Texture.BORDER_CORNER_TOP);

		for (int j = 0; j < endX; ++j) {
			this.blit(stack, startX + 1 + j * 18, startY, i, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(stack, startX + 1 + j * 18, startY + endY * 19 - 1, i, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int k = 0; k < endY; ++k) {
			this.blit(stack, startX, startY + k * 18 + 1, i, Texture.BORDER_VERTICAL);
			this.blit(stack, startX + endX * 18 + 1, startY + k * 18 + 1, i, Texture.BORDER_VERTICAL);
		}

		this.blit(stack, startX, startY + endY * 19 - 1, i, Texture.BORDER_CORNER_BOTTOM);
		this.blit(stack, startX + endX * 18 + 1, startY + endY * 18 + 2, i, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(PoseStack stack, int x, int y, int idk, Texture texture) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
		GuiComponent.blit(stack, x, y, idk, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
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