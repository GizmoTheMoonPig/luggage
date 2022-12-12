package com.gizmo.luggage;

import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
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
		HitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			if (!level.isClientSide()) {
				LuggageEntity luggage = Registries.EntityRegistry.LUGGAGE.get().create(level);
				if (luggage != null) {
					luggage.moveTo(blockPos);
					luggage.tame(player);
					luggage.restoreFromStack(player.getItemInHand(hand));
					level.addFreshEntity(luggage);
				}
				if (!player.getAbilities().instabuild) {
					player.getItemInHand(hand).shrink(1);
				}
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	public static Stream<ItemStack> getContentsForToolTip(ItemStack stack) {
		CompoundTag compoundtag = stack.getTag();
		if (compoundtag == null) {
			return Stream.empty();
		} else {
			ListTag listtag = compoundtag.getList(LuggageEntity.INVENTORY_TAG, 10);
			return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		NonNullList<ItemStack> nonnulllist = NonNullList.create();
		getContentsForToolTip(stack).forEach(nonnulllist::add);
		return nonnulllist.isEmpty() ? Optional.empty() : Optional.of(new Tooltip(nonnulllist, stack));
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
		super.fillItemCategory(tab, stacks);

		if (this.allowedIn(tab)) {
			ItemStack item = new ItemStack(this);
			CompoundTag tag = new CompoundTag();
			tag.putBoolean(LuggageEntity.EXTENDED_TAG, true);
			item.setTag(tag);
			stacks.add(item);
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
