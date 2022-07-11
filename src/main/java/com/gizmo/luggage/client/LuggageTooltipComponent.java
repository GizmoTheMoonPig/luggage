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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Modified version of ClientBundleTooltip
public class LuggageTooltipComponent implements ClientTooltipComponent {

	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
	private final NonNullList<ItemStack> items;
	private final boolean extended;

	public LuggageTooltipComponent(LuggageItem.Tooltip tooltip) {
		this.items = tooltip.stacks();
		this.extended = tooltip.stack().getTag() != null && tooltip.stack().getTag().contains("Extended") && tooltip.stack().getTag().getBoolean("Extended");
	}

	@Override
	public int getHeight() {
		return (extended ? 6 : 3) * 18 + 2 + 4;
	}

	@Override
	public int getWidth(Font font) {
		return 9 * 18 + 2;
	}

	@Override
	public void renderImage(Font font, int x, int y, PoseStack ms, ItemRenderer renderer, int idk) {
		int i = this.gridSizeX();
		int j = this.gridSizeY();
		int k = 0;

		for (int l = 0; l < j; ++l) {
			for (int i1 = 0; i1 < i; ++i1) {
				int j1 = x + i1 * 18 + 1;
				int k1 = y + l * 18 + 1;
				this.renderSlot(j1, k1, k++, font, ms, renderer, idk);
			}
		}

		this.drawBorder(x, y, i, j, ms, idk);
	}

	private void renderSlot(int x, int y, int slot, Font font, PoseStack ms, ItemRenderer renderer, int idk) {
		if (slot >= this.items.size()) {
			this.blit(ms, x, y, idk, Texture.SLOT);
		} else {
			ItemStack itemstack = this.items.get(slot);
			this.blit(ms, x, y, idk, Texture.SLOT);
			renderer.renderAndDecorateItem(itemstack, x + 1, y + 1, slot);
			renderer.renderGuiItemDecorations(font, itemstack, x + 1, y + 1);
			if (slot == 0) {
				AbstractContainerScreen.renderSlotHighlight(ms, x + 1, y + 1, idk);
			}

		}
	}

	private void drawBorder(int x, int y, int x2, int y2, PoseStack ms, int idk) {
		this.blit(ms, x, y, idk, Texture.BORDER_CORNER_TOP);
		this.blit(ms, x + x2 * 18 + 1, y, idk, Texture.BORDER_CORNER_TOP);

		for (int i = 0; i < x2; ++i) {
			this.blit(ms, x + 1 + i * 18, y, idk, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(ms, x + 1 + i * 18, y + y2 * 18 + 1, idk, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int j = 0; j < y2; ++j) {
			this.blit(ms, x, y + j * 18 + 1, idk, Texture.BORDER_VERTICAL);
			this.blit(ms, x + x2 * 18 + 1, y + j * 18 + 1, idk, Texture.BORDER_VERTICAL);
		}

		//this.blit(ms, x, y + y2 * 19, idk, Texture.BORDER_CORNER_BOTTOM);
		//this.blit(ms, x + x2 * 18 + 1, y + y2 * 18, idk, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(PoseStack ms, int x, int y, int idk, Texture texture) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
		GuiComponent.blit(ms, x, y, idk, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
	}

	private int gridSizeX() {
		return 9;
	}

	private int gridSizeY() {
		return this.extended ? 6 : 3;
	}

	@OnlyIn(Dist.CLIENT)
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