package com.damir00109.laststand.mixin;

import com.damir00109.laststand.client.LastStandClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class CameraLockMixin {

    @Inject(method = "setCameraType(Lnet/minecraft/client/CameraType;)V", at = @At("HEAD"), cancellable = true)
    private void laststand$lockPerspectiveWhileDowned(CameraType cameraType, CallbackInfo ci) {
        if (cameraType != CameraType.FIRST_PERSON && LastStandClient.isCameraLocked()) {
            ci.cancel();
        }
    }
}
