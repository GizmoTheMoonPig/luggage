package com.gizmo.luggage.entity;

import com.gizmo.luggage.LuggageRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class EnderLuggage extends AbstractLuggage {

	public EnderLuggage(EntityType<? extends TamableAnimal> type, Level level) {
		super(type, level);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && this.tickCount % 5 == 0) {
			for (int i = 0; i < 2; ++i) {
				int j = this.getRandom().nextInt(2) * 2 - 1;
				int k = this.getRandom().nextInt(2) * 2 - 1;
				double d0 = this.getX() + 0.25D * (double) j;
				double d1 = this.getY() + this.getRandom().nextDouble();
				double d2 = this.getZ() + 0.25D * (double) k;
				double d3 = this.getRandom().nextFloat() * (float) j;
				double d4 = (this.getRandom().nextFloat() - 0.5D) * 0.125D;
				double d5 = this.getRandom().nextFloat() * (float) k;
				this.level().addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
			}
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason == RemovalReason.KILLED) {
			this.spawnAnim();
			this.playSound(LuggageRegistries.LUGGAGE_KILLED.get(), 8.0F, 1.0F);
		}
		super.remove(reason);
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(LuggageRegistries.ENDER_LUGGAGE_ITEM.get());
	}

	@Override
	public void onOpenMenu(Player player) {
		this.level().gameEvent(player, GameEvent.CONTAINER_OPEN, player.blockPosition());
		if (this.getSoundCooldown() == 0) {
			this.playSound(SoundEvents.ENDER_CHEST_OPEN, 0.5F, this.getRandom().nextFloat() * 0.1F + 0.9F);
			this.setSoundCooldown(5);
		}
		player.openMenu(new SimpleMenuProvider((id, inventory, cPlayer) -> ChestMenu.threeRows(id, inventory, cPlayer.getEnderChestInventory()), this.getDisplayName()));
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(LuggageRegistries.ENDER_LUGGAGE_ITEM.get());
	}
}
