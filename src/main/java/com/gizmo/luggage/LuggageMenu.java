package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class LuggageMenu extends Container {

	private final IInventory luggageContainer;
	private final LuggageEntity luggage;
	private final int containerRows;

	public LuggageMenu(int id, PlayerInventory inventory, IInventory container, LuggageEntity luggage) {
		super(ContainerType.GENERIC_9x6, id);
		checkContainerSize(container, luggage.hasExtendedInventory()  ? 54 : 27);
		this.luggageContainer = container;
		this.luggage = luggage;
		this.containerRows = luggage.hasExtendedInventory() ? 6 : 3;
		container.startOpen(inventory.player);
		int i = (this.containerRows - 4) * 18;

		for(int j = 0; j < this.containerRows; ++j) {
			for(int k = 0; k < 9; ++k) {
				this.addSlot(new LuggageSlot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for(int l = 0; l < 3; ++l) {
			for(int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
			}
		}

		for(int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 161 + i));
		}

	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return !this.luggage.hasInventoryChanged(this.luggageContainer) &&
				this.luggageContainer.stillValid(player) &&
				this.luggage.isAlive() &&
				this.luggage.distanceTo(player) < 8.0F;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int id) {
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
	public void removed(PlayerEntity player) {
		super.removed(player);
		this.luggageContainer.stopOpen(player);
	}

	public static class LuggageSlot extends Slot {

		public LuggageSlot(IInventory container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return !(stack.getItem() == Registries.ItemRegistry.LUGGAGE.get()) && !(stack.getItem() == Items.SHULKER_BOX);
		}
	}
}
