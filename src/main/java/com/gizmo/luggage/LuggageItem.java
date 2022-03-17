package com.gizmo.luggage;

import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LuggageItem extends Item {

	public LuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		RayTraceResult result = getPlayerPOVHitResult(level, player, RayTraceContext.FluidMode.NONE);
		if (result.getType() == RayTraceResult.Type.BLOCK) {
			Vector3d blockPos = result.getLocation();
			if (!level.isClientSide) {
				LuggageEntity luggage = Registries.EntityRegistry.LUGGAGE.get().create(level);
				if(luggage != null){
					luggage.moveTo(blockPos);
					luggage.tame(player);
					luggage.restoreFromStack(player.getItemInHand(hand));
					level.addFreshEntity(luggage);
				}
				if(!player.abilities.instabuild) {
					player.getItemInHand(hand).shrink(1);
				}
			}
			return ActionResult.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
		}
		return ActionResult.pass(player.getItemInHand(hand));
	}
}
