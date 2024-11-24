package com.gizmo.luggage.item;

import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class LuggageItem extends AbstractLuggageItem<Luggage> {

	public LuggageItem(Properties properties) {
		super(LuggageRegistries.LUGGAGE, properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		boolean insertedAny = false;
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			BlockPos pos = result.getBlockPos();
			//attempt to dump items into a container if shift is held and a container is targeted
			if (!level.isClientSide() && player.isSecondaryUseActive() && level.getBlockState(BlockPos.containing(blockPos)).hasBlockEntity()) {
				BlockEntity be = level.getBlockEntity(result.getBlockPos());
				IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, result.getBlockPos(), level.getBlockState(pos), be, result.getDirection());
				if (cap != null) {
					SimpleContainer newInv = new SimpleContainer(stack.has(LuggageRegistries.EXTENDED) ? 54 : 27);
					for (ItemStack stack1 : this.getContents(stack)) {
						for (int slot = 0; slot < cap.getSlots(); slot++) {
							if (cap.insertItem(slot, stack1, true) != stack1) {
								stack1 = cap.insertItem(slot, stack1, false);
								insertedAny = true;
								break;
							}
						}
						newInv.addItem(stack1);
					}
					if (insertedAny) {
						stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(newInv.getItems()));
					}
				}
			}

			//if we dont insert any items fire super to spawn the luggage
			return insertedAny ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide()) : super.use(level, player, hand);
		} else if (result.getType() == HitResult.Type.MISS && player.isSecondaryUseActive()) {
			//throw all items into the air, similar to a bundle, when holding shift and targeting no block
			if (this.dropContents(stack, player)) {
				player.awardStat(Stats.ITEM_USED.get(this));
				return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
			}
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void onLuggagePlaced(ItemStack stack, Luggage luggage) {
		if (stack.has(LuggageRegistries.EXTENDED)) {
			luggage.setExtendedInventory(true);
		}

		if (stack.has(DataComponents.CONTAINER)) {
			stack.get(DataComponents.CONTAINER).copyInto(luggage.getInventory().getItems());
			if (luggage.getInventory().getContainerSize() > 27) {
				luggage.setExtendedInventory(true);
			}
		}

		luggage.setFetchCooldown(20);
		super.onLuggagePlaced(stack, luggage);
	}

	private Iterable<ItemStack> getContents(ItemStack stack) {
		if (!stack.has(DataComponents.CONTAINER)) {
			return new ArrayList<>();
		} else {
			return stack.get(DataComponents.CONTAINER).nonEmptyItems();
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		NonNullList<ItemStack> nonnulllist = NonNullList.create();
		this.getContents(stack).forEach(nonnulllist::add);
		return nonnulllist.isEmpty() ? Optional.empty() : Optional.of(new Tooltip(nonnulllist, stack));
	}

	private boolean dropContents(ItemStack stack, Player player) {
		if (!stack.has(DataComponents.CONTAINER)) {
			return false;
		} else {
			if (player instanceof ServerPlayer) {
				stack.get(DataComponents.CONTAINER).nonEmptyItems().forEach(stack1 -> player.drop(stack1, true));
			}

			stack.remove(DataComponents.CONTAINER);
			return true;
		}
	}

	@Override
	public void onDestroyed(ItemEntity entity, DamageSource source) {
		//drop all items if luggage is destroyed for any reason whatsoever
		ItemUtils.onContainerDestroyed(entity, this.getContents(entity.getItem()));
	}

	public record Tooltip(NonNullList<ItemStack> stacks, ItemStack stack) implements TooltipComponent {

	}
}
