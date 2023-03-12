package com.gizmo.luggage;

import com.gizmo.luggage.entity.Luggage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LuggageMenu extends AbstractContainerMenu {

	private final Container luggageContainer;
	private final Luggage luggage;
	private final int containerRows;

	public LuggageMenu(int id, Inventory inventory, Container container, Luggage luggage) {
		super(MenuType.GENERIC_9x6, id);
		checkContainerSize(container, luggage.hasExtendedInventory() ? 54 : 27);
		this.luggageContainer = container;
		this.luggage = luggage;
		this.containerRows = luggage.hasExtendedInventory() ? 6 : 3;
		container.startOpen(inventory.player);
		int i = (this.containerRows - 4) * 18;

		for (int j = 0; j < this.containerRows; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlot(new LuggageSlot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 161 + i));
		}

	}

	@Override
	public boolean stillValid(Player player) {
		return !this.luggage.hasInventoryChanged(this.luggageContainer) &&
				this.luggageContainer.stillValid(player) &&
				this.luggage.isAlive() &&
				this.luggage.distanceTo(player) < 8.0F;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int id) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(id);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (id < this.containerRows * 9) {
				if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.luggage.setInventoryOpen(false);
		this.luggageContainer.stopOpen(player);
	}

	public static class LuggageSlot extends Slot {

		public LuggageSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return !stack.is(Registries.ItemRegistry.LUGGAGE.get()) && stack.getItem().canFitInsideContainerItems();
		}
	}
}
