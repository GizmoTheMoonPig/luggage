package com.gizmo.luggage.mixin.client;

import com.gizmo.luggage.client.ClientEvents;
import net.minecraft.client.KeyboardHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("TAIL"))
    private void luggage$onKeyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
        ClientEvents.ClientFabricEvents.commandTheCreatures(pKey, pScanCode, pAction, pModifiers);
    }
}
