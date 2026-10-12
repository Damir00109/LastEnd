package com.damir00109.laststand.client;

import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.data.LastStandDataAttachments;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = LastStand.MOD_ID, value = Dist.CLIENT)
public final class LastStandClient {
    private static boolean wasDowned = false;
    private static CameraType previousCameraType = CameraType.FIRST_PERSON;
    private static float crawlPhase = 0.0F;
    private static float prevCrawlPhase = 0.0F;
    private static int crawlDirection = 1;
    private static Field noJumpDelayField;

    private static final float CRAWL_SPEED = 0.03F;

    static {
        try {
            noJumpDelayField = LivingEntity.class.getDeclaredField("noJumpDelay");
            noJumpDelayField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            LastStand.LOGGER.warn("[Last Stand] Could not access LivingEntity.noJumpDelay");
        }
    }

    public static float getCrawlPhase() {
        return crawlPhase;
    }

    public static float getPrevCrawlPhase() {
        return prevCrawlPhase;
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            resetState();
            return;
        }

        boolean isDowned = LastStandDataAttachments.get(mc.player).isDowned();

        if (isDowned && !wasDowned) {
            previousCameraType = mc.options.getCameraType();
            mc.player.setForcedPose(Pose.SWIMMING);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
            LastStand.LOGGER.debug("[Last Stand] Client entered DOWNED");
        } else if (!isDowned && wasDowned) {
            mc.player.setForcedPose(null);
            mc.options.setCameraType(previousCameraType);
            crawlPhase = 0.0F;
            prevCrawlPhase = 0.0F;
            crawlDirection = 1;
        }

        if (isDowned) {
            setNoJumpDelay(mc.player, 10);
        }
    }

    public static boolean isCameraLocked() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && LastStandDataAttachments.get(mc.player).isDowned();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        resetState();
    }

    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        resetState();
    }

    private static void resetState() {
        wasDowned = false;
        crawlPhase = 0.0F;
        prevCrawlPhase = 0.0F;
        crawlDirection = 1;
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (!LastStandDataAttachments.get(mc.player).isDowned()) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (LastStandDataAttachments.get(mc.player).isDowned()) {
            event.getInput().shiftKeyDown = false;
            mc.player.setShiftKeyDown(false);
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        boolean isDowned = LastStandDataAttachments.get(mc.player).isDowned();

        if (isDowned) {
            boolean isMoving = mc.player.input.forwardImpulse != 0.0F
                    || mc.player.input.leftImpulse != 0.0F;

            prevCrawlPhase = crawlPhase;
            if (isMoving) {
                crawlPhase += CRAWL_SPEED * crawlDirection;
                if (crawlPhase >= 1.0F) {
                    crawlPhase = 1.0F;
                    crawlDirection = -1;
                } else if (crawlPhase <= 0.0F) {
                    crawlPhase = 0.0F;
                    crawlDirection = 1;
                }
            } else {
                crawlPhase -= CRAWL_SPEED;
                if (crawlPhase < 0.0F) {
                    crawlPhase = 0.0F;
                }
                if (crawlPhase == 0.0F) {
                    crawlDirection = 1;
                }
            }
        } else if (wasDowned) {
            crawlPhase = 0.0F;
            prevCrawlPhase = 0.0F;
            crawlDirection = 1;
        }

        wasDowned = isDowned;
    }

    private static void setNoJumpDelay(LocalPlayer player, int value) {
        if (noJumpDelayField != null) {
            try {
                noJumpDelayField.setInt(player, value);
            } catch (IllegalAccessException e) {
                LastStand.LOGGER.warn("[Last Stand] Failed to set noJumpDelay");
            }
        }
    }
}
