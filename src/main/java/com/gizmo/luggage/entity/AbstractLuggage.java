package com.gizmo.luggage.entity;

import com.gizmo.luggage.LuggageRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class AbstractLuggage extends TamableAnimal {

	private int soundCooldown = 15;
	private static final TicketType<Integer> LUGGAGE_UNLOAD = TicketType.create("luggage_unload", Integer::compareTo, 20);

	protected AbstractLuggage(EntityType<? extends TamableAnimal> type, Level level) {
		super(type, level);
		this.setPathfindingMalus(PathType.LEAVES, -1.0F);
		this.setPathfindingMalus(PathType.FENCE, -1.0F);
		this.setPathfindingMalus(PathType.COCOA, -1.0F);
		this.setPathfindingMalus(PathType.WATER, 0.0F);
		this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.1D, 7.0F, 1.0F));
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 0.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.35D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.soundCooldown > 0) {
			this.soundCooldown--;
		}
	}

	@Override
	public void onRemovedFromLevel() {
		if (this.level() != null && this.level() instanceof ServerLevel server && !this.isInSittingPose() && this.getOwner() != null) {
			server.getChunkSource().addRegionTicket(LUGGAGE_UNLOAD, new ChunkPos(this.blockPosition()), 2, this.getId());
		}
		super.onRemovedFromLevel();
	}

	public int getSoundCooldown() {
		return this.soundCooldown;
	}

	public void setSoundCooldown(int cooldown) {
		this.soundCooldown = cooldown;
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
		return null;
	}

	//override tame logic to prevent the advancement for taming a mob to be granted
	@Override
	public void tame(Player player) {
		this.setTame(true, false);
		this.setOwnerUUID(player.getUUID());
	}

	//no attacking
	@Override
	public boolean wantsToAttack(LivingEntity owner, LivingEntity target) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean isInvulnerable() {
		return true;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}

	@Override
	public void setHealth(float health) {
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	@Override
	public void knockback(double x, double y, double z) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	public boolean addEffect(MobEffectInstance instance, @Nullable Entity entity) {
		return false;
	}

	@Override
	public boolean causeFallDamage(float dist, float mult, DamageSource source) {
		return false;
	}

	@Override
	public void checkDespawn() {
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.9F;
	}

	@Override
	public double getFluidJumpThreshold() {
		return 0.2F;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public boolean isSteppingCarefully() {
		return true;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(LuggageRegistries.LUGGAGE_STEP.get(), 0.1F, 0.7F + (this.getRandom().nextFloat() * 0.5F));
	}

	@Override
	public boolean shouldTryTeleportToOwner() {
		LivingEntity livingentity = this.getOwner();
		return livingentity != null && this.distanceToSqr(this.getOwner()) >= 1600.0D;
	}
}
