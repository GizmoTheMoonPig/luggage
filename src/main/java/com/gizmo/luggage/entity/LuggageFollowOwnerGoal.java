package com.gizmo.luggage.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

//Inspired by FollowOwnerGoal
//Luggage won't follow if its trying to fetch an item, and the teleport distance was greatly increased
public class LuggageFollowOwnerGoal extends Goal {
	private final LuggageEntity luggage;
	private LivingEntity owner;
	private final LevelReader level;
	private final double speedModifier;
	private final PathNavigation navigation;
	private int timeToRecalcPath;
	private final float stopDistance;
	private final float startDistance;
	private float oldWaterCost;

	public LuggageFollowOwnerGoal(LuggageEntity luggage, double speed, float startDist, float stopDist) {
		this.luggage = luggage;
		this.level = luggage.level;
		this.speedModifier = speed;
		this.navigation = luggage.getNavigation();
		this.startDistance = startDist;
		this.stopDistance = stopDist;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	public boolean canUse() {
		LivingEntity livingentity = this.luggage.getOwner();
		List<ItemEntity> items = this.luggage.getLevel().getEntitiesOfClass(ItemEntity.class, this.luggage.getBoundingBox().inflate(8.0D), item ->
				(item.isOnGround() || item.isInWater()) &&
						this.luggage.hasLineOfSight(item) &&
						this.luggage.getInventory().canAddItem(item.getItem()) &&
						item.getItem().getItem().canFitInsideContainerItems());
		if (livingentity == null || livingentity.isSpectator() || livingentity.getPose() == Pose.SLEEPING) {
			return false;
		} else if (this.luggage.isInSittingPose() || this.luggage.isTryingToFetchItem()) {
			return false;
		} else if (this.luggage.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
			return false;
		} else {
			this.owner = livingentity;
			List<ItemEntity> revisedItems = new ArrayList<>();
			if (!items.isEmpty()) {
				for (ItemEntity item : items) {
					//if it's out of reach it doesn't count
					Path toPath = this.navigation.createPath(item, 1);
					if (toPath != null && toPath.canReach()) {
						revisedItems.add(item);
					}
				}
			}
			return revisedItems.isEmpty();
		}
	}

	public boolean canContinueToUse() {
		if (this.navigation.isDone()) {
			return false;
		} else {
			return !(this.luggage.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
		}
	}

	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.luggage.getPathfindingMalus(BlockPathTypes.WATER);
		this.luggage.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.luggage.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
	}

	public void tick() {
		this.luggage.getLookControl().setLookAt(this.owner, 10.0F, (float) this.luggage.getMaxHeadXRot());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = this.adjustedTickDelay(10);
			if (!this.luggage.isLeashed() && !this.luggage.isPassenger()) {
				//1600 = 40 blocks
				if (this.luggage.distanceToSqr(this.owner) >= 1600.0D) {
					this.teleportToOwner();
				} else {
					this.navigation.moveTo(this.owner, this.speedModifier);
				}

			}
		}
	}

	private void teleportToOwner() {
		BlockPos blockpos = this.owner.blockPosition();

		for (int i = 0; i < 10; ++i) {
			int j = this.randomIntInclusive(-3, 3);
			int k = this.randomIntInclusive(-1, 1);
			int l = this.randomIntInclusive(-3, 3);
			boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
			if (flag) {
				return;
			}
		}

	}

	private boolean maybeTeleportTo(int x, int y, int z) {
		if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
			return false;
		} else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
			return false;
		} else {
			this.luggage.moveTo((double) x + 0.5D, y, (double) z + 0.5D, this.luggage.getYRot(), this.luggage.getXRot());
			this.navigation.stop();
			return true;
		}
	}

	private boolean canTeleportTo(BlockPos pos) {
		BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
		if (blockpathtypes != BlockPathTypes.WALKABLE) {
			return false;
		} else {
			BlockPos blockpos = pos.subtract(this.luggage.blockPosition());
			return this.level.noCollision(this.luggage, this.luggage.getBoundingBox().move(blockpos));
		}
	}

	private int randomIntInclusive(int min, int max) {
		return this.luggage.getRandom().nextInt(max - min + 1) + min;
	}
}
