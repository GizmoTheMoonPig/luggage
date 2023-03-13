package com.gizmo.luggage.entity;

import com.gizmo.luggage.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class EnderLuggage extends AbstractLuggage {

	public EnderLuggage(EntityType<? extends TamableAnimal> type, Level level) {
		super(type, level);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.getLevel().isClientSide() && this.tickCount % 5 == 0) {
			for (int i = 0; i < 2; ++i) {
				int j = this.getRandom().nextInt(2) * 2 - 1;
				int k = this.getRandom().nextInt(2) * 2 - 1;
				double d0 = this.getX() + 0.25D * (double) j;
				double d1 = this.getY() + this.getRandom().nextDouble();
				double d2 = this.getZ() + 0.25D * (double) k;
				double d3 = this.getRandom().nextFloat() * (float) j;
				double d4 = (this.getRandom().nextFloat() - 0.5D) * 0.125D;
				double d5 = this.getRandom().nextFloat() * (float) k;
				this.getLevel().addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
			}
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.isAlive()) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;

			if (player.isShiftKeyDown()) {
				if (this.getOwner() == player) {
					if (!this.getLevel().isClientSide()) {
						ItemStack luggageItem = new ItemStack(Registries.ItemRegistry.ENDER_LUGGAGE.get());
						if (player.getInventory().add(luggageItem)) {
							this.discard();
							this.playSound(SoundEvents.ITEM_PICKUP, 0.5F, this.getRandom().nextFloat() * 0.1F + 0.9F);
						}
					}
					return InteractionResult.sidedSuccess(this.getLevel().isClientSide());
				} else {
					player.displayClientMessage(Component.translatable("entity.luggage.player_doesnt_own").withStyle(ChatFormatting.DARK_RED), true);
					return InteractionResult.CONSUME;
				}
			} else {
				this.getLevel().gameEvent(player, GameEvent.CONTAINER_OPEN, player.blockPosition());
				//prevents sound from playing 4 times (twice on server only). Apparently interactAt fires 4 times????
				if (this.getSoundCooldown() == 0) {
					this.playSound(SoundEvents.ENDER_CHEST_OPEN, 0.5F, this.getRandom().nextFloat() * 0.1F + 0.9F);
					this.setSoundCooldown(5);
				}
				player.openMenu(new SimpleMenuProvider((id, inventory, cPlayer) -> ChestMenu.threeRows(id, inventory, player.getEnderChestInventory()), this.getTypeName()));
				return InteractionResult.sidedSuccess(this.getLevel().isClientSide());
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason == RemovalReason.KILLED) {
			this.spawnAnim();
			this.playSound(Registries.SoundRegistry.LUGGAGE_KILLED.get(), 8.0F, 1.0F);
		}
		super.remove(reason);
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Registries.ItemRegistry.ENDER_LUGGAGE.get());
	}
}
