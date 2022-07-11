package com.gizmo.luggage;

import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.LuggageEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LuggageItem extends Item {

	public LuggageItem(Properties properties) {
		super(properties);
		BuiltinItemRendererRegistry.INSTANCE.register(this, new LuggageItemRenderer());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		HitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			if (!level.isClientSide) {
				LuggageEntity luggage = Registries.EntityRegistry.LUGGAGE.create(level);
				if(luggage != null){
					luggage.moveTo(blockPos);
					luggage.tame(player);
					luggage.restoreFromStack(player.getItemInHand(hand));
					level.addFreshEntity(luggage);
				}
				if(!player.getAbilities().instabuild) {
					player.getItemInHand(hand).shrink(1);
				}
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
		super.fillItemCategory(tab, stacks);

		if(allowdedIn(tab)) {
			ItemStack item = new ItemStack(this);
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("Extended", true);
			item.setTag(tag);
			stacks.add(item);
		}
	}
}
