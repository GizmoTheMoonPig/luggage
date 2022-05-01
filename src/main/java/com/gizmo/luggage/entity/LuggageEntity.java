package com.gizmo.luggage.entity;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LuggageEntity extends PathfinderMob implements OwnableEntity, ContainerListener {

	private static final EntityDataAccessor<Boolean> EXTENDED = SynchedEntityData.defineId(LuggageEntity.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Byte> TAME_FLAGS = SynchedEntityData.defineId(LuggageEntity.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(LuggageEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	private SimpleContainer inventory;
	private LazyOptional<?> itemHandler = null;
	private int soundCooldown = 15;
	private boolean tryingToFetchItem;

	public LuggageEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
		this.createInventory();
		this.setPathfindingMalus(BlockPathTypes.LEAVES, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new LuggagePickupItemGoal(this));
		this.goalSelector.addGoal(2, new LuggageFollowOwnerGoal(this, 1.1D, 7.0F, 1.0F, false));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(EXTENDED, false);
		this.entityData.define(OWNER_ID, Optional.empty());
		this.entityData.define(TAME_FLAGS, (byte) 0);
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 0.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.4D);
	}

	//-----------------------------------------//
	//                SAVE DATA                //
	//-----------------------------------------//

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		ListTag listtag = new ListTag();

		tag.putBoolean("Extended", this.hasExtendedInventory());

		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundtag = new CompoundTag();
				compoundtag.putByte("Slot", (byte) i);
				itemstack.save(compoundtag);
				listtag.add(compoundtag);
			}
		}

		tag.put("Items", listtag);

		if (this.getOwnerUUID() != null) {
			tag.putUUID("Owner", this.getOwnerUUID());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ListTag listtag = tag.getList("Items", 10);

		this.setExtendedInventory(tag.getBoolean("Extended"));

		for (int i = 0; i < listtag.size(); ++i) {
			CompoundTag compoundtag = listtag.getCompound(i);
			int j = compoundtag.getByte("Slot") & 255;
			if (j < this.inventory.getContainerSize()) {
				this.inventory.setItem(j, ItemStack.of(compoundtag));
			}
		}

		UUID uuid;
		if (tag.hasUUID("Owner")) {
			uuid = tag.getUUID("Owner");
		} else {
			String s = tag.getString("Owner");
			uuid = OldUsersConverter.convertMobOwnerIfNecessary(Objects.requireNonNull(this.getServer()), s);
		}

		if (uuid != null) {
			try {
				this.setOwnerUUID(uuid);
				this.setTame(true);
			} catch (Throwable throwable) {
				this.setTame(false);
			}
		}
	}

	//------------------------------------------//
	//              ITEM TO ENTITY              //
	//------------------------------------------//

	private ItemStack convertToItem() {

		ItemStack luggageItem = new ItemStack(Registries.ItemRegistry.LUGGAGE.get());
		CompoundTag tag = new CompoundTag();

		if(this.hasExtendedInventory()) {
			tag.putBoolean("Extended", this.hasExtendedInventory());
		}

		if(!this.inventory.isEmpty()) {
			tag.put("Inventory", this.inventory.createTag());
		}

		if(!tag.isEmpty()) {
			luggageItem.setTag(tag);
		}

		Component nameTag = this.getCustomName();
		if (nameTag != null && !nameTag.getString().isEmpty()) {
			luggageItem.setHoverName(nameTag);
		}

		return luggageItem;
	}

	public void restoreFromStack(@Nonnull ItemStack stack) {
		//im not this stupid, but just in case
		if (!stack.is(Registries.ItemRegistry.LUGGAGE.get())) return;

		CompoundTag tag = stack.getTag();

		if(tag != null && tag.contains("Extended")) {
			this.setExtendedInventory(tag.getBoolean("Extended"));
		}

		if (tag != null && tag.contains("Inventory")) {
			inventory.fromTag(tag.getList("Inventory", 10));
			if (inventory.getContainerSize() > 27) {
				this.setExtendedInventory(true);
			}
		}

		if (stack.hasCustomHoverName()) {
			this.setCustomName(stack.getHoverName());
		}
	}

	//------------------------------------------//
	//            INVENTORY HANDLING            //
	//------------------------------------------//

	private void createInventory() {
		SimpleContainer simplecontainer = this.inventory;
		this.inventory = new SimpleContainer(this.hasExtendedInventory() ? 54 : 27);
		if (simplecontainer != null) {
			simplecontainer.removeListener(this);
			int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

			for (int j = 0; j < i; ++j) {
				ItemStack itemstack = simplecontainer.getItem(j);
				if (!itemstack.isEmpty()) {
					this.inventory.setItem(j, itemstack.copy());
				}
			}
		}

		this.inventory.addListener(this);
		this.itemHandler = LazyOptional.of(() -> new InvWrapper(this.inventory));
	}

	public SimpleContainer getInventory() {
		return this.inventory;
	}

	public boolean hasExtendedInventory() {
		return this.entityData.get(EXTENDED);
	}

	public void setExtendedInventory(boolean extended) {
		this.entityData.set(EXTENDED, extended);
		this.createInventory();
	}

	@Override
	public void containerChanged(Container p_18983_) {
		//I dont think I need this for anything
	}

	public boolean hasInventoryChanged(Container container) {
		return this.inventory != container;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (this.isAlive() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler != null)
			return itemHandler.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		if (itemHandler != null) {
			LazyOptional<?> oldHandler = itemHandler;
			itemHandler = null;
			oldHandler.invalidate();
		}
	}

	//------------------------------------------//
	//               TAMING STUFF               //
	//------------------------------------------//

	@Nullable
	@Override
	public UUID getOwnerUUID() {
		return this.entityData.get(OWNER_ID).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uuid) {
		this.entityData.set(OWNER_ID, Optional.ofNullable(uuid));
	}


	@Nullable
	@Override
	public LivingEntity getOwner() {
		try {
			UUID uuid = this.getOwnerUUID();
			return uuid == null ? null : this.level.getPlayerByUUID(uuid);
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void setTame(boolean p_21836_) {
		byte b0 = this.entityData.get(TAME_FLAGS);
		if (p_21836_) {
			this.entityData.set(TAME_FLAGS, (byte) (b0 | 4));
		} else {
			this.entityData.set(TAME_FLAGS, (byte) (b0 & -5));
		}
	}

	public void tame(Player player) {
		this.setTame(true);
		this.setOwnerUUID(player.getUUID());
	}

	//------------------------------------------//
	//                   MISC                   //
	//------------------------------------------//

	public boolean isTryingToFetchItem() {
		return this.tryingToFetchItem;
	}

	public void setTryingToFetchItem(boolean fetch) {
		this.tryingToFetchItem = fetch;
	}

	public int getSoundCooldown() {
		return this.soundCooldown;
	}

	public void setSoundCooldown(int cooldown) {
		this.soundCooldown = cooldown;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if(this.soundCooldown > 0) {
			this.soundCooldown--;
		}
	}

	@Override
	public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
		if (this.isAlive()) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;

			if (stack.isEmpty()) {
				if (player.isShiftKeyDown()) {
					if (!level.isClientSide()) {
						ItemStack luggageItem = this.convertToItem();
						if (player.getInventory().add(luggageItem)) {
							this.discard();
							this.playSound(SoundEvents.ITEM_PICKUP, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
						}
					}
				} else {
					level.gameEvent(player, GameEvent.CONTAINER_OPEN, player.blockPosition());
					//prevents sound from playing 4 times (twice on server only). Apparently interactAt fires 4 times????
					if(this.soundCooldown == 0) {
						this.playSound(SoundEvents.CHEST_OPEN, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
						this.soundCooldown = 5;
					}
					if (!level.isClientSide()) {
						ServerPlayer sp = (ServerPlayer) player;
						if (sp.containerMenu != sp.inventoryMenu) sp.closeContainer();

						sp.nextContainerCounter();
						LuggageNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new OpenLuggageScreenPacket(sp.containerCounter, this.getId()));
						sp.containerMenu = new LuggageMenu(sp.containerCounter, sp.getInventory(), this.inventory, this);
						sp.initMenu(sp.containerMenu);
						MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(sp, sp.containerMenu));
					}
				}
			}
		}

		return super.interactAt(player, vec, hand);
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
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
	public void thunderHit(ServerLevel level, LightningBolt lightningBolt) {
		if (!this.hasExtendedInventory()) {
			this.setExtendedInventory(true);
		} else {
			super.thunderHit(level, lightningBolt);
		}
	}

	@Override
	public void knockback(double x, double y, double z) {}

	@Override
	protected void pushEntities() {}

	@Override
	public boolean addEffect(MobEffectInstance instance, @Nullable Entity entity) {
		return false;
	}

	@Override
	public boolean causeFallDamage(float dist, float mult, DamageSource source) {
		return false;
	}

	@Override
	public void checkDespawn() {}

	@Override
	public void kill() {
		this.getInventory().removeAllItems().forEach(this::spawnAtLocation);
		this.spawnAnim();
		this.playSound(Registries.SoundRegistry.LUGGAGE_KILLED, 2.0F, 1.0F);
		this.remove(RemovalReason.KILLED);
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Nullable
	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Registries.ItemRegistry.LUGGAGE.get());
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.95F;
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
	public boolean canBeLeashed(Player player) {
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
		this.playSound(Registries.SoundRegistry.LUGGAGE_STEP, 0.15F, 0.7F + (random.nextFloat() * 0.5F));
	}
}
