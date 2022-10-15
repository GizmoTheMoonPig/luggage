package com.gizmo.luggage.mixin.client;

import com.gizmo.luggage.entity.LuggageEntity;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public HitResult hitResult;
    @Shadow @Nullable public MultiPlayerGameMode gameMode;
    @Unique
    private final ThreadLocal<Boolean> shouldCancel = ThreadLocal.withInitial(() -> false);

    //completely prevent Luggage from blocking attacks. This will swing directly through it, and will even allow you to hit other mobs behind it
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;", shift = At.Shift.BEFORE))
    private void luggage$fireEvent(CallbackInfoReturnable<Boolean> cir) {
        if (hitResult instanceof EntityHitResult result && result.getEntity() instanceof LuggageEntity) {
            shouldCancel.set(true);
            Vec3 vec3 = player.getEyePosition(1.0F);
            Vec3 vec31 = player.getViewVector(1.0F);
            double d0 = (double) gameMode.getPickRange() + 1.5D;
            double d1 = hitResult.getLocation().distanceToSqr(vec3) + 8.0D;
            Vec3 vec32 = vec3.add(vec31.x() * d0, vec31.y() * d0, vec31.z() * d0);
            AABB aabb = player.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(player, vec3, vec32, aabb, (entity) -> !entity.isSpectator() && entity.isPickable() && !(entity instanceof LuggageEntity), d1);
            if (entityhitresult != null) {
                gameMode.attack(player, entityhitresult.getEntity());
            }
        }
    }

    @WrapWithCondition(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean luggage$modifiyResult(MultiPlayerGameMode gameMode, Player player, Entity entity) {
        return !shouldCancel.get();
    }
}
