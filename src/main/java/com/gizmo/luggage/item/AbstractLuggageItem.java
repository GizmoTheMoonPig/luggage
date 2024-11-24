package com.gizmo.luggage.item;

import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class AbstractLuggageItem<T extends AbstractLuggage> extends Item {

	private final Supplier<EntityType<T>> luggage;

	public AbstractLuggageItem(Supplier<EntityType<T>> entity, Properties properties) {
		super(properties);
		this.luggage = entity;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		ItemStack stack = player.getItemInHand(hand);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 vec = result.getLocation();
			if (!level.isClientSide()) {
				T entity = this.luggage.get().create(level);
				if (entity != null) {
					entity.moveTo(vec);
					entity.tame(player);
					this.onLuggagePlaced(stack, entity);
					level.addFreshEntity(entity);
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}
				}
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		return InteractionResultHolder.pass(stack);
	}

	public void onLuggagePlaced(ItemStack stack, T luggage) {
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			luggage.setCustomName(stack.getHoverName());
		}
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	@Nullable
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}
}
