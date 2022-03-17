package com.gizmo.luggage.entity;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LuggageEntity extends CreatureEntity implements IInventoryChangedListener {

	private static final DataParameter<Boolean> EXTENDED = EntityDataManager.defineId(LuggageEntity.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Byte> TAME_FLAGS = EntityDataManager.defineId(LuggageEntity.class, DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.defineId(LuggageEntity.class, DataSerializers.OPTIONAL_UUID);

	private Inventory inventory;
	private LazyOptional<?> itemHandler = null;
	public int lastSound = 0;
	public boolean tryingToFetchItem;

	public LuggageEntity(EntityType<? extends CreatureEntity> type, World level) {
		super(type, level);
		this.createInventory();
		this.setPathfindingMalus(PathNodeType.LEAVES, -1.0F);
		this.setPathfindingMalus(PathNodeType.FENCE, -1.0F);
		this.setPathfindingMalus(PathNodeType.COCOA, -1.0F);
		this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
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

	public static AttributeModifierMap.MutableAttribute registerAttributes() {
		return MobEntity.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 0.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.4D);
	}

	//-----------------------------------------//
	//                SAVE DATA                //
	//-----------------------------------------//

	@Override
	public void addAdditionalSaveData(CompoundNBT tag) {
		super.addAdditionalSaveData(tag);
		ListNBT listtag = new ListNBT();

		tag.putBoolean("Extended", this.hasExtendedInventory());

		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundNBT compoundtag = new CompoundNBT();
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
	public void readAdditionalSaveData(CompoundNBT tag) {
		super.readAdditionalSaveData(tag);
		ListNBT listtag = tag.getList("Items", 10);

		this.setExtendedInventory(tag.getBoolean("Extended"));

		for (int i = 0; i < listtag.size(); ++i) {
			CompoundNBT compoundtag = listtag.getCompound(i);
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
			uuid = PreYggdrasilConverter.convertMobOwnerIfNecessary(Objects.requireNonNull(this.getServer()), s);
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
		CompoundNBT tag = new CompoundNBT();

		if(this.hasExtendedInventory()) {
			tag.putBoolean("Extended", this.hasExtendedInventory());
		}

		if(!this.inventory.isEmpty()) {
			tag.put("Inventory", this.inventory.createTag());
		}

		if(!tag.isEmpty()) {
			luggageItem.setTag(tag);
		}

		ITextComponent nameTag = this.getCustomName();
		if (nameTag != null && !nameTag.getString().isEmpty()) {
			luggageItem.setHoverName(nameTag);
		}

		return luggageItem;
	}

	public void restoreFromStack(@Nonnull ItemStack stack) {
		//im not this stupid, but just in case
		if (!(stack.getItem() == Registries.ItemRegistry.LUGGAGE.get())) return;

		CompoundNBT tag = stack.getTag();

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
		Inventory simplecontainer = this.inventory;
		this.inventory = new Inventory(this.hasExtendedInventory() ? 54 : 27);
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

	public Inventory getInventory() {
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
	public void containerChanged(IInventory p_18983_) {
		//I dont think I need this for anything
	}

	public boolean hasInventoryChanged(IInventory container) {
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
	public UUID getOwnerUUID() {
		return this.entityData.get(OWNER_ID).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uuid) {
		this.entityData.set(OWNER_ID, Optional.ofNullable(uuid));
	}


	@Nullable
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

	public void tame(PlayerEntity player) {
		this.setTame(true);
		this.setOwnerUUID(player.getUUID());
	}

	//------------------------------------------//
	//                   MISC                   //
	//------------------------------------------//

	@Override
	public void aiStep() {
		super.aiStep();
		this.lastSound++;
	}

	@Override
	public ActionResultType interactAt(PlayerEntity player, Vector3d vec, Hand hand) {
		if (this.isAlive()) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.getItem() == Items.NAME_TAG) return ActionResultType.PASS;

			if (stack.isEmpty()) {
				if (player.isShiftKeyDown()) {
					if (!level.isClientSide) {
						ItemStack luggageItem = this.convertToItem();
						if (player.inventory.add(luggageItem)) {
							this.remove();
							this.playSound(SoundEvents.ITEM_PICKUP, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
						}
					}
				} else {
					this.playSound(SoundEvents.CHEST_OPEN, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
					if (!level.isClientSide) {
						ServerPlayerEntity sp = (ServerPlayerEntity) player;
						if (sp.containerMenu != sp.inventoryMenu) sp.closeContainer();

						sp.nextContainerCounter();
						LuggageNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new OpenLuggageScreenPacket(sp.containerCounter, this.getId()));
						sp.containerMenu = new LuggageMenu(sp.containerCounter, sp.inventory, this.inventory, this);
						sp.containerMenu.addSlotListener(sp);
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
	public void thunderHit(ServerWorld level, LightningBoltEntity lightningBolt) {
		if (!this.hasExtendedInventory()) {
			this.setExtendedInventory(true);
		} else {
			super.thunderHit(level, lightningBolt);
		}
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
	public boolean canBeLeashed(PlayerEntity player) {
		return false;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(Registries.SoundRegistry.LUGGAGE_STEP, 0.15F, 0.7F + (random.nextFloat() * 0.5F));
	}
}
