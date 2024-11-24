package com.gizmo.luggage.entity;

import com.gizmo.luggage.LuggageMenu;
import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.entity.ai.LuggagePickupItemGoal;
import com.gizmo.luggage.network.OpenLuggageScreenPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class Luggage extends AbstractLuggage implements ContainerListener {

	private static final EntityDataAccessor<Boolean> EXTENDED = SynchedEntityData.defineId(Luggage.class, EntityDataSerializers.BOOLEAN);

	public static final String EXTENDED_TAG = "Extended";

	private SimpleContainer inventory;
	private int fetchCooldown = 0;
	private boolean tryingToFetchItem;
	private boolean isInventoryOpen;

	public Luggage(EntityType<? extends TamableAnimal> type, Level level) {
		super(type, level);
		this.createInventory();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new LuggagePickupItemGoal(this));
		this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.1D, 7.0F, 1.0F) {
			@Override
			public boolean canUse() {
				if (super.canUse()) {
					List<ItemEntity> items = Luggage.this.level().getEntitiesOfClass(ItemEntity.class, Luggage.this.getBoundingBox().inflate(8.0D), item ->
							(item.onGround() || item.isInWater()) &&
									Luggage.this.hasLineOfSight(item) &&
									Luggage.this.getInventory().canAddItem(item.getItem()) &&
									item.getItem().getItem().canFitInsideContainerItems());

					if (Luggage.this.isInSittingPose() || Luggage.this.isTryingToFetchItem()) {
						return false;
					} else {
						List<ItemEntity> revisedItems = new ArrayList<>();
						if (!items.isEmpty()) {
							for (ItemEntity item : items) {
								//if it's out of reach it doesn't count
								Path toPath = Luggage.this.navigation.createPath(item, 1);
								if (toPath != null && toPath.canReach()) {
									revisedItems.add(item);
								}
							}
						}
						return revisedItems.isEmpty();
					}
				}
				return false;
			}
		});
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(EXTENDED, false);
	}

	//-----------------------------------------//
	//                SAVE DATA                //
	//-----------------------------------------//

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		ListTag listtag = new ListTag();

		tag.putBoolean(EXTENDED_TAG, this.hasExtendedInventory());

		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundtag = new CompoundTag();
				compoundtag.putByte("Slot", (byte) i);
				listtag.add(itemstack.save(this.level().registryAccess(), compoundtag));
			}
		}

		tag.put("Items", listtag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ListTag listtag = tag.getList("Items", 10);

		this.setExtendedInventory(tag.getBoolean(EXTENDED_TAG));

		for (int i = 0; i < listtag.size(); ++i) {
			CompoundTag compoundtag = listtag.getCompound(i);
			int j = compoundtag.getByte("Slot") & 255;
			if (j < this.inventory.getContainerSize()) {
				this.inventory.setItem(j, ItemStack.parseOptional(this.level().registryAccess(), compoundtag));
			}
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
	}

	public SimpleContainer getInventory() {
		return this.inventory;
	}

	public boolean hasExtendedInventory() {
		return this.getEntityData().get(EXTENDED);
	}

	public void setExtendedInventory(boolean extended) {
		this.getEntityData().set(EXTENDED, extended);
		this.createInventory();
	}

	@Override
	public void containerChanged(Container container) {
		//I don't think I need this for anything
	}

	public boolean hasInventoryChanged(Container container) {
		return this.inventory != container;
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

	public int getFetchCooldown() {
		return this.fetchCooldown;
	}

	public void setFetchCooldown(int cooldown) {
		this.fetchCooldown = cooldown;
	}

	public boolean isInventoryOpen() {
		return this.isInventoryOpen;
	}

	public void setInventoryOpen(boolean open) {
		this.isInventoryOpen = open;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.fetchCooldown > 0) {
			this.fetchCooldown--;
		}

		if (this.isInSittingPose() && this.tickCount % 10 == 0) {
			this.level().addParticle(ParticleTypes.NOTE,
					this.getX() + (this.getRandom().nextDouble() - 0.5D) * this.getBbWidth(),
					this.getY() + this.getEyeHeight() + 0.25D,
					this.getZ() + (this.getRandom().nextDouble() - 0.5D) * this.getBbWidth(),
					this.getRandom().nextDouble(),
					0.0D,
					0.0D);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.isAlive() && this.getOwner() != player && !player.isShiftKeyDown()) {
			player.displayClientMessage(Component.translatable("entity.luggage.player_doesnt_own").withStyle(ChatFormatting.DARK_RED), true);
			return InteractionResult.CONSUME;
		}
		return super.mobInteract(player, hand);
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
	public void remove(RemovalReason reason) {
		if (reason == RemovalReason.KILLED) {
			this.getInventory().removeAllItems().forEach(this::spawnAtLocation);
			this.spawnAnim();
			this.playSound(LuggageRegistries.LUGGAGE_KILLED.get(), 8.0F, 1.0F);
		}
		super.remove(reason);
	}

	@Override
	public ItemStack createItem() {
		ItemStack luggageItem = new ItemStack(LuggageRegistries.LUGGAGE_ITEM.get());

		if (this.hasExtendedInventory()) {
			luggageItem.set(LuggageRegistries.EXTENDED, Unit.INSTANCE);
		}

		if (!this.inventory.isEmpty()) {
			luggageItem.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.inventory.getItems()));
		}

		return luggageItem;
	}

	@Override
	public void onOpenMenu(Player player) {
		this.level().gameEvent(player, GameEvent.CONTAINER_OPEN, player.blockPosition());
		if (this.getSoundCooldown() == 0) {
			this.playSound(SoundEvents.CHEST_OPEN, 0.5F, this.getRandom().nextFloat() * 0.1F + 0.9F);
			this.setSoundCooldown(5);
		}
		if (!this.level().isClientSide()) {
			ServerPlayer sp = (ServerPlayer) player;
			if (sp.containerMenu != sp.inventoryMenu) {
				sp.closeContainer();
			}

			sp.nextContainerCounter();
			PacketDistributor.sendToPlayer(sp, new OpenLuggageScreenPacket(sp.containerCounter, this.getId()));
			sp.containerMenu = new LuggageMenu(sp.containerCounter, sp.getInventory(), this.inventory, this);
			sp.initMenu(sp.containerMenu);
			this.isInventoryOpen = true;
			NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(sp, sp.containerMenu));
		}
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(LuggageRegistries.LUGGAGE_ITEM.get());
	}
}
