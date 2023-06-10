package com.gizmo.luggage.entity.ai;

import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.entity.Luggage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class LuggagePickupItemGoal extends Goal {

	private final Luggage luggage;
	private final PathNavigation navigation;
	@Nullable
	private ItemEntity targetItem = null;

	public LuggagePickupItemGoal(Luggage luggage) {
		this.luggage = luggage;
		this.navigation = luggage.getNavigation();
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		//we only want the luggage to pick up items if it isn't on a cooldown or forced to sit
		if (this.luggage.getFetchCooldown() > 0 || this.luggage.isInSittingPose() || this.luggage.isInventoryOpen() || !this.navigation.isDone())
			return false;

		//sort through items, get the closest one
		List<ItemEntity> items = this.luggage.level().getEntitiesOfClass(ItemEntity.class, this.luggage.getBoundingBox().inflate(16.0D), item ->
				(item.onGround() || item.isInWater()) &&
						this.luggage.hasLineOfSight(item) &&
						this.luggage.getInventory().canAddItem(item.getItem()) &&
						item.getItem().getItem().canFitInsideContainerItems());
		items.sort(Comparator.comparingDouble(this.luggage::distanceToSqr));

		for (ItemEntity item : items) {
			//please, only go after items you can actually reach
			Path toPath = this.navigation.createPath(item, 1);
			if (toPath != null && toPath.canReach()) {
				this.targetItem = item;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.luggage.isAlive() && !this.navigation.isDone() && !this.navigation.isStuck() && this.targetItem != null && !this.targetItem.isRemoved();
	}

	@Override
	public void start() {
		if (this.targetItem != null) {
			this.navigation.moveTo(this.targetItem, 1.2D);
			this.luggage.setTryingToFetchItem(true);
		}
	}

	@Override
	public void stop() {
		this.luggage.setTryingToFetchItem(false);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.luggage.level().isClientSide()) {
			if (this.targetItem != null && this.luggage.distanceToSqr(this.targetItem.position()) < 4.0D) {
				ItemStack item = this.targetItem.getItem();
				if (this.luggage.getInventory().canAddItem(this.targetItem.getItem())) {
					if (this.luggage.getSoundCooldown() == 0) {
						boolean isFood = item.isEdible();
						this.luggage.playSound(isFood ? LuggageRegistries.SoundRegistry.LUGGAGE_EAT_FOOD.get() : LuggageRegistries.SoundRegistry.LUGGAGE_EAT_ITEM.get(),
								0.5F, 1.0F + (this.luggage.getRandom().nextFloat() * 0.2F));
						this.luggage.setSoundCooldown(15);
					}

					//stole this from Villager.pickUpItem lol
					SimpleContainer simplecontainer = this.luggage.getInventory();
					boolean flag = simplecontainer.canAddItem(item);
					if (!flag) {
						return;
					}

					this.luggage.onItemPickup(this.targetItem);
					this.luggage.gameEvent(GameEvent.EAT, this.luggage);
					this.luggage.take(this.targetItem, item.getCount());
					ItemStack consumedStack = simplecontainer.addItem(item);
					if (consumedStack.isEmpty()) {
						this.targetItem.discard();
					} else {
						item.setCount(consumedStack.getCount());
					}
				}
			}
		}
	}
}
