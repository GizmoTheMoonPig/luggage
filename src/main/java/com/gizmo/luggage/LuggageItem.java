package com.gizmo.luggage;

import com.gizmo.luggage.client.LuggageItemRenderer;
import com.gizmo.luggage.entity.LuggageEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class LuggageItem extends Item {

	public LuggageItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		HitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (result.getType() == HitResult.Type.BLOCK) {
			Vec3 blockPos = result.getLocation();
			if (!level.isClientSide) {
				LuggageEntity luggage = Registries.EntityRegistry.LUGGAGE.get().create(level);
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
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
				return new LuggageItemRenderer();
			}
		});
	}
}