package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.Registries;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.item.LuggageItem;
import com.gizmo.luggage.network.CallLuggagePetsPacket;
import com.gizmo.luggage.network.LuggageNetworkHandler;
import com.gizmo.luggage.network.SitNearbyLuggagesPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = LuggageMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(LuggageMod.ID, "luggage"), "main");
	private static KeyMapping callKey;
	private static KeyMapping waitKey;

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(LUGGAGE, LuggageModel::create);
	}

	@SubscribeEvent
	public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(Registries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
		event.registerEntityRenderer(Registries.EntityRegistry.ENDER_LUGGAGE.get(), EnderLuggageRenderer::new);
	}

	@SubscribeEvent
	public static void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(LuggageItem.Tooltip.class, LuggageTooltipComponent::new);
	}

	@SubscribeEvent
	public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
		callKey = new KeyMapping(
				"keybind.luggage.call",
				KeyConflictContext.IN_GAME,
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");

		waitKey = new KeyMapping(
				"keybind.luggage.wait",
				KeyConflictContext.IN_GAME,
				KeyModifier.ALT,
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_GRAVE_ACCENT,
				"key.categories.misc");
		event.register(getCallKey());
		event.register(getWaitKey());
	}

	public static KeyMapping getCallKey() {
		return callKey;
	}

	public static KeyMapping getWaitKey() {
		return waitKey;
	}

	@Mod.EventBusSubscriber(modid = LuggageMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientForgeEvents {

		@SubscribeEvent
		public static void commandTheCreatures(InputEvent.Key event) {
			if (event.getAction() == GLFW.GLFW_PRESS && Minecraft.getInstance().player != null) {
				float pitch = Minecraft.getInstance().player.getRandom().nextFloat() * 0.1F + 0.9F;
				if (event.getKey() == getCallKey().getKey().getValue() && getCallKey().consumeClick()) {
					Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE_CALL.get(), 1.0F, pitch);
					LuggageNetworkHandler.CHANNEL.sendToServer(new CallLuggagePetsPacket(Minecraft.getInstance().player.getId()));
				} else if (event.getKey() == getWaitKey().getKey().getValue() && getWaitKey().consumeClick()) {
					Minecraft.getInstance().player.playSound(Registries.SoundRegistry.WHISTLE_WAIT.get(), 0.85F, pitch);
					LuggageNetworkHandler.CHANNEL.sendToServer(new SitNearbyLuggagesPacket(Minecraft.getInstance().player.getId()));
				}
			}
		}

		//completely prevent Luggage from blocking attacks. This will swing directly through it, and will even allow you to hit other mobs behind it
		//fire last in case any other mod tries to do something like this
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void attackThroughLuggage(InputEvent.InteractionKeyMappingTriggered event) {
			if (event.isAttack() && Minecraft.getInstance().hitResult instanceof EntityHitResult result && result.getEntity() instanceof AbstractLuggage) {
				event.setCanceled(true);
				event.setSwingHand(true);
				Player player = Minecraft.getInstance().player;
				Vec3 vec3 = player.getEyePosition(1.0F);
				Vec3 vec31 = player.getViewVector(1.0F);
				double d0 = (double) Minecraft.getInstance().gameMode.getPickRange() + 1.5D;
				double d1 = Minecraft.getInstance().hitResult.getLocation().distanceToSqr(vec3) + 8.0D;
				Vec3 vec32 = vec3.add(vec31.x() * d0, vec31.y() * d0, vec31.z() * d0);
				AABB aabb = player.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
				EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(player, vec3, vec32, aabb, (entity) -> !entity.isSpectator() && entity.isPickable() && !(entity instanceof AbstractLuggage), d1);
				if (entityhitresult != null) {
					Minecraft.getInstance().gameMode.attack(player, entityhitresult.getEntity());
				}
			}
		}
	}
}
