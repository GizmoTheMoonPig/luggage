package com.gizmo.luggage.item;

import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.EnderLuggage;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnderLuggageItem extends Item {
	public EnderLuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		HitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			if (!level.isClientSide()) {
				EnderLuggage luggage = LuggageRegistries.EntityRegistry.ENDER_LUGGAGE.get().create(level);
				if (luggage != null) {
					luggage.moveTo(blockPos);
					luggage.tame(player);
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
