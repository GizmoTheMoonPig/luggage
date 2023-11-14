package com.gizmo.luggage.item;

import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LuggageItem extends Item {

	public LuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		boolean insertedAny = false;
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			//attempt to dump items into a container if shift is held and a container is targeted
			if (!level.isClientSide() && player.isSecondaryUseActive() && level.getBlockState(BlockPos.containing(blockPos)).hasBlockEntity()) {
				BlockEntity be = level.getBlockEntity(result.getBlockPos());
				if (be != null && be.getCapability(ForgeCapabilities.ITEM_HANDLER, result.getDirection()).resolve().isPresent()) {
					CompoundTag tag = stack.getTag();
					SimpleContainer newInv = new SimpleContainer(tag != null && tag.contains(Luggage.EXTENDED_TAG) ? 54 : 27);
					IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, result.getDirection()).resolve().get();
					for (ItemStack stack1 : this.getContents(stack).toList()) {
						for (int slot = 0; slot < handler.getSlots(); slot++) {
							if (handler.insertItem(slot, stack1, true) != stack1) {
								stack1 = handler.insertItem(slot, stack1, false);
								insertedAny = true;
								break;
							}
						}
						newInv.addItem(stack1);
					}
					if (insertedAny && tag != null) {
						tag.put(Luggage.INVENTORY_TAG, newInv.createTag());
					}
				}
			}
			if (!insertedAny) {
				if (!level.isClientSide()) {
					Luggage luggage = LuggageRegistries.EntityRegistry.LUGGAGE.get().create(level);
					if (luggage != null) {
						luggage.moveTo(blockPos);
						luggage.tame(player);
						luggage.restoreFromStack(stack);
						level.addFreshEntity(luggage);
					}
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}
				}
			}

			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		//throw all items into the air, similar to a bundle, when holding shift and targeting no block
		} else if (result.getType() == HitResult.Type.MISS && player.isSecondaryUseActive()) {
			if (this.dropContents(stack, player)) {
				player.awardStat(Stats.ITEM_USED.get(this));
				return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
			}
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	private Stream<ItemStack> getContents(ItemStack stack) {
		CompoundTag compoundtag = stack.getTag();
		if (compoundtag == null) {
			return Stream.empty();
		} else {
			ListTag listtag = compoundtag.getList(Luggage.INVENTORY_TAG, 10);
			return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		NonNullList<ItemStack> nonnulllist = NonNullList.create();
		this.getContents(stack).forEach(nonnulllist::add);
		return nonnulllist.isEmpty() ? Optional.empty() : Optional.of(new Tooltip(nonnulllist, stack));
	}

	private boolean dropContents(ItemStack stack, Player player) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(Luggage.INVENTORY_TAG)) {
			return false;
		} else {
			if (player instanceof ServerPlayer) {
				ListTag listtag = tag.getList(Luggage.INVENTORY_TAG, 10);

				for(int i = 0; i < listtag.size(); ++i) {
					CompoundTag stackTag = listtag.getCompound(i);
					ItemStack itemstack = ItemStack.of(stackTag);
					player.drop(itemstack, true);
				}
			}

			stack.removeTagKey(Luggage.INVENTORY_TAG);
			return true;
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
		return slot == EquipmentSlot.HEAD;
	}

	@Override
	@Nullable
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}

	@Override
	public void onDestroyed(ItemEntity entity, DamageSource source) {
		//drop all items if luggage is destroyed for any reason whatsoever
		ItemUtils.onContainerDestroyed(entity, this.getContents(entity.getItem()));
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new LuggageItemRenderer();
			}
		});
	}

	public record Tooltip(NonNullList<ItemStack> stacks, ItemStack stack) implements TooltipComponent {

	}
}
