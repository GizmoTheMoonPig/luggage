package com.gizmo.luggage.client;

import com.gizmo.luggage.LuggageMod;
import com.gizmo.luggage.LuggageRegistries;
import com.gizmo.luggage.entity.AbstractLuggage;
import com.gizmo.luggage.item.LuggageItem;
import com.gizmo.luggage.network.CallLuggagePacket;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {
	public static final ModelLayerLocation LUGGAGE = new ModelLayerLocation(new ResourceLocation(LuggageMod.ID, "luggage"), "main");
	private static final KeyMapping CALL_KEY = new KeyMapping(
			"keybind.luggage.call",
			KeyConflictContext.IN_GAME,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_GRAVE_ACCENT,
			"key.categories.misc");
	private static final KeyMapping WAIT_KEY = new KeyMapping(
			"keybind.luggage.wait",
			KeyConflictContext.IN_GAME,
			KeyModifier.ALT,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_GRAVE_ACCENT,
			"key.categories.misc");

	public static void init(IEventBus bus) {
		bus.addListener(RegisterKeyMappingsEvent.class, event -> {
			event.register(CALL_KEY);
			event.register(WAIT_KEY);
		});
		bus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> event.registerLayerDefinition(LUGGAGE, LuggageModel::create));
		bus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
			event.registerEntityRenderer(LuggageRegistries.EntityRegistry.LUGGAGE.get(), LuggageRenderer::new);
			event.registerEntityRenderer(LuggageRegistries.EntityRegistry.ENDER_LUGGAGE.get(), EnderLuggageRenderer::new);
		});
		bus.addListener(RegisterClientTooltipComponentFactoriesEvent.class, event -> event.register(LuggageItem.Tooltip.class, LuggageTooltipComponent::new));
		NeoForge.EVENT_BUS.addListener(ClientEvents::commandTheCreatures);
		NeoForge.EVENT_BUS.addListener(ClientEvents::attackThroughLuggage);
	}

	private static void commandTheCreatures(InputEvent.Key event) {
		if (event.getAction() == GLFW.GLFW_PRESS && Minecraft.getInstance().player != null) {
			float pitch = Minecraft.getInstance().player.getRandom().nextFloat() * 0.1F + 0.9F;
			if (event.getKey() == CALL_KEY.getKey().getValue() && CALL_KEY.consumeClick()) {
				Minecraft.getInstance().player.playSound(LuggageRegistries.SoundRegistry.WHISTLE_CALL.get(), 1.0F, pitch);
				PacketDistributor.SERVER.noArg().send(new CallLuggagePacket());
			} else if (event.getKey() == WAIT_KEY.getKey().getValue() && WAIT_KEY.consumeClick()) {
				Minecraft.getInstance().player.playSound(LuggageRegistries.SoundRegistry.WHISTLE_WAIT.get(), 0.85F, pitch);
				PacketDistributor.SERVER.noArg().send(new SitNearbyLuggagesPacket());
			}
		}
	}

	//completely prevent Luggage from blocking attacks. This will swing directly through it, and will even allow you to hit other mobs behind it
	//fire last in case any other mod tries to do something like this
	private static void attackThroughLuggage(InputEvent.InteractionKeyMappingTriggered event) {
		if (!event.isCanceled() && event.isAttack() && Minecraft.getInstance().hitResult instanceof EntityHitResult result && result.getEntity() instanceof AbstractLuggage) {
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
