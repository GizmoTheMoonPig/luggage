package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.entity.LuggageEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LuggageScreen extends ContainerScreen<LuggageMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
	private final int containerRows;

	public LuggageScreen(LuggageMenu menu, PlayerInventory inventory, LuggageEntity entity) {
		super(menu, inventory, entity.getDisplayName());
		this.passEvents = false;
		this.containerRows = entity.hasExtendedInventory() ? 6 : 3;
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	public void render(MatrixStack ms, int x, int y, float partialTicks) {
		this.renderBackground(ms);
		super.render(ms, x, y, partialTicks);
		this.renderTooltip(ms, x, y);
	}

	protected void renderBg(MatrixStack ms, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(CONTAINER_BACKGROUND);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(ms, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
		this.blit(ms, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
	}
}
