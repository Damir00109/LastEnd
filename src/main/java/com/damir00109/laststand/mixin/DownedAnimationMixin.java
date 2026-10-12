package com.damir00109.laststand.mixin;

import com.damir00109.laststand.client.LastStandClient;
import com.damir00109.laststand.data.LastStandDataAttachments;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class DownedAnimationMixin {

    private static final float ARM_HEAD = Mth.PI - 0.5F;
    private static final float ARM_SIDE = Mth.HALF_PI;
    private static final float LEG_SPREAD = 15.0F * (float) Math.PI / 180.0F;

    @Inject(
        method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
        at = @At("RETURN")
    )
    private void onSetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && LastStandDataAttachments.get(player).isDowned()) {
            HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

            float partial = ageInTicks - entity.tickCount;
            float t = Mth.lerp(partial, LastStandClient.getPrevCrawlPhase(), LastStandClient.getCrawlPhase());

            float rightArmZ = Mth.lerp(t, ARM_HEAD, ARM_SIDE);
            float leftArmZ = Mth.lerp(t, -ARM_HEAD, -ARM_SIDE);

            float rightLegZ = Mth.lerp(t, 0.0F, LEG_SPREAD);
            float leftLegZ = Mth.lerp(t, 0.0F, -LEG_SPREAD);

            model.rightArm.x = -5.0F;
            model.rightArm.y = 2.0F;
            model.rightArm.z = 0.0F;
            model.rightArm.xRot = 0.0F;
            model.rightArm.yRot = 0.0F;
            model.rightArm.zRot = rightArmZ;

            model.leftArm.x = 5.0F;
            model.leftArm.y = 2.0F;
            model.leftArm.z = 0.0F;
            model.leftArm.xRot = 0.0F;
            model.leftArm.yRot = 0.0F;
            model.leftArm.zRot = leftArmZ;

            model.rightLeg.x = -1.9F;
            model.rightLeg.y = 12.0F;
            model.rightLeg.z = 0.0F;
            model.rightLeg.xRot = 0.0F;
            model.rightLeg.yRot = 0.0F;
            model.rightLeg.zRot = rightLegZ;

            model.leftLeg.x = 1.9F;
            model.leftLeg.y = 12.0F;
            model.leftLeg.z = 0.0F;
            model.leftLeg.xRot = 0.0F;
            model.leftLeg.yRot = 0.0F;
            model.leftLeg.zRot = leftLegZ;
        }
    }
}
