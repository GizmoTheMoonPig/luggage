package com.gizmo.luggage.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.world.IWorldReader;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.BlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;

import java.util.EnumSet;

//[VanillaCopy] of FollowOwnerGoal, but changed the entity to not be hardcoded to TamableAnimal
//it also wont follow if its trying to fetch an item, and the teleport distance was greatly increased
public class LuggageFollowOwnerGoal extends Goal {
	private final LuggageEntity luggage;
	private LivingEntity owner;
	private final IWorldReader level;
	private final double speedModifier;
	private final PathNavigator navigation;
	private int timeToRecalcPath;
	private final float stopDistance;
	private final float startDistance;
	private float oldWaterCost;
	private final boolean canFly;

	public LuggageFollowOwnerGoal(LuggageEntity luggage, double speed, float startDist, float stopDist, boolean fly) {
		this.luggage = luggage;
		this.level = luggage.level;
		this.speedModifier = speed;
		this.navigation = luggage.getNavigation();
		this.startDistance = startDist;
		this.stopDistance = stopDist;
		this.canFly = fly;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	public boolean canUse() {
		LivingEntity livingentity = this.luggage.getOwner();
		if (livingentity == null) {
			return false;
		} else if (livingentity.isSpectator()) {
			return false;
		} else if (this.luggage.tryingToFetchItem) {
			return false;
		} else if (this.luggage.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
			return false;
		} else {
			this.owner = livingentity;
			return true;
		}
	}

	public boolean canContinueToUse() {
		if (this.navigation.isDone()) {
			return false;
		} else {
			return !(this.luggage.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
		}
	}

	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.luggage.getPathfindingMalus(PathNodeType.WATER);
		this.luggage.setPathfindingMalus(PathNodeType.WATER, 0.0F);
	}

	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.luggage.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
	}

	public void tick() {
		this.luggage.getLookControl().setLookAt(this.owner, 10.0F, (float)this.luggage.getMaxHeadXRot());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
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

		for(int i = 0; i < 10; ++i) {
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
		if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
			return false;
		} else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
			return false;
		} else {
			this.luggage.moveTo((double)x + 0.5D, y, (double)z + 0.5D, this.luggage.yRot, this.luggage.xRot);
			this.navigation.stop();
			return true;
		}
	}

	private boolean canTeleportTo(BlockPos pos) {
		PathNodeType blockpathtypes = WalkNodeProcessor.getBlockPathTypeStatic(this.level, pos.mutable());
		if (blockpathtypes != PathNodeType.WALKABLE) {
			return false;
		} else {
			BlockState blockstate = this.level.getBlockState(pos.below());
			if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
				return false;
			} else {
				BlockPos blockpos = pos.subtract(this.luggage.blockPosition());
				return this.level.noCollision(this.luggage, this.luggage.getBoundingBox().move(blockpos));
			}
		}
	}

	private int randomIntInclusive(int min, int max) {
		return this.luggage.getRandom().nextInt(max - min + 1) + min;
	}
}
