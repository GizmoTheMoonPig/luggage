package com.gizmo.luggage.item;

import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class AbstractLuggageItem extends Item {
	public AbstractLuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		ItemStack stack = player.getItemInHand(hand);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 vec = result.getLocation();
			if (!level.isClientSide()) {
				AbstractLuggage entity = this.getLuggageEntity().create(level);
				if (entity != null) {
					entity.moveTo(vec);
					entity.tame(player);
					if (entity instanceof Luggage luggage) {
						luggage.restoreFromStack(stack);
					}
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

	public abstract EntityType<? extends AbstractLuggage> getLuggageEntity();

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
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
}
