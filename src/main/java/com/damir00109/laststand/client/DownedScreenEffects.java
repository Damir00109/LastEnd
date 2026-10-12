package com.damir00109.laststand.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Set;

@EventBusSubscriber(modid = LastStand.MOD_ID, value = Dist.CLIENT)
public final class DownedScreenEffects {
    private static final ResourceLocation BLOOD_MAIN =
            ResourceLocation.fromNamespaceAndPath(LastStand.MOD_ID, "textures/gui/blood_vignette_a.png");
    private static final ResourceLocation BLOOD_DETAIL =
            ResourceLocation.fromNamespaceAndPath(LastStand.MOD_ID, "textures/gui/blood_vignette_b.png");

    private static final Set<ResourceLocation> HIDDEN_LAYERS = Set.of(
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.CROSSHAIR,
            VanillaGuiLayers.SELECTED_ITEM_NAME,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.EFFECTS,
            VanillaGuiLayers.VEHICLE_HEALTH);

    private static final long TICK_MILLIS = 50L;
    private static final float TIMER_SCALE = 4.0F;
    private static final int BLOOD_PADDING = 28;

    private static int syncedTicks = -1;
    private static long syncedAtNanos;
    private static int syncedReviveProgress = -1;
    private static long reviveSyncedAtNanos;
    private static int maxTicks;
    private static long lastBeatNanos;
    private static int beatCountdown;
    private static int dubCountdown;

    private DownedScreenEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!isDowned()) {
            reset();
            return;
        }

        remainingMillis(LastStandDataAttachments.get(Minecraft.getInstance().player));

        if (--beatCountdown <= 0) {
            beat(0.85F + 0.5F * intensity(), 1.0F);
            beatCountdown = beatIntervalTicks();
            dubCountdown = 3;
        } else if (dubCountdown > 0 && --dubCountdown == 0) {
            beat(0.5F + 0.35F * intensity(), 0.82F);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        PlayerDownedData data = LastStandDataAttachments.get(mc.player);
        if (!data.isDowned()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        float intensity = intensity();
        float pulse = pulseAt(System.nanoTime());
        double time = System.nanoTime() / 1_000_000_000.0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        float base = 0.20F + 0.26F * intensity;
        float amp = 0.16F + 0.34F * intensity;
        drawBlood(graphics, BLOOD_MAIN,
                Mth.clamp(base + amp * pulse, 0.0F, 1.0F),
                (float) Math.sin(time * 0.7) * 6.0F,
                (float) Math.cos(time * 0.9) * 5.0F);
        drawBlood(graphics, BLOOD_DETAIL,
                Mth.clamp(base * 0.75F + amp * 0.55F * pulse, 0.0F, 1.0F),
                (float) Math.cos(time * 0.5) * 10.0F,
                (float) Math.sin(time * 0.6) * 8.0F);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        drawTimer(graphics, mc, remainingMillis(data));

        if (data.isBeingRevived()) {
            drawReviveInfo(graphics, mc, data);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (isDowned() && HIDDEN_LAYERS.contains(event.getName())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!isDowned()) {
            return;
        }

        float pulse = pulseAt(System.nanoTime());
        float amp = (0.10F + 1.20F * intensity()) * (0.35F + 0.65F * pulse);
        double time = System.nanoTime() / 1_000_000_000.0;

        float yaw = (float) (Math.sin(time * 7.3) * 0.6 + Math.sin(time * 15.7) * 0.4);
        float pitch = (float) (Math.sin(time * 9.1 + 1.7) * 0.6 + Math.sin(time * 19.3) * 0.4);
        float roll = (float) (Math.sin(time * 5.5 + 0.9) * 0.7);

        event.setYaw(event.getYaw() + yaw * amp * 0.7F);
        event.setPitch(event.getPitch() + pitch * amp * 0.5F);
        event.setRoll(event.getRoll() + roll * amp);
    }

    private static void drawBlood(GuiGraphics graphics, ResourceLocation texture, float alpha, float driftX, float driftY) {
        if (alpha <= 0.01F) {
            return;
        }

        int width = graphics.guiWidth() + BLOOD_PADDING * 2;
        int height = graphics.guiHeight() + BLOOD_PADDING * 2;

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(texture, (int) (-BLOOD_PADDING + driftX), (int) (-BLOOD_PADDING + driftY), 0, 0.0F, 0.0F,
                width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTimer(GuiGraphics graphics, Minecraft mc, long remainingMillis) {
        long totalSeconds = remainingMillis / 1000L;
        String text = String.format("%02d:%02d.%03d", totalSeconds / 60L, totalSeconds % 60L, remainingMillis % 1000L);

        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F, 0.0F);
        graphics.pose().scale(TIMER_SCALE, TIMER_SCALE, 1.0F);
        graphics.drawString(mc.font, text, -mc.font.width(text) / 2, -4, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    private static void drawReviveInfo(GuiGraphics graphics, Minecraft mc, PlayerDownedData data) {
        String time = formatSeconds(reviveRemainingMillis(data));
        String text = Component.translatable("gui.laststand.revive_you", data.getReviverName(), time).getString();

        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 26;
        graphics.drawString(mc.font, text, x - mc.font.width(text) / 2, y, 0xFF9BE89B, true);
    }

    private static String formatSeconds(long millis) {
        return String.format("%.2f", millis / 1000.0);
    }

    public static long reviveRemainingMillis(PlayerDownedData data) {
        int progress = data.getReviveProgress();
        int total = data.getReviveTotal();
        if (total <= 0) {
            return 0L;
        }

        if (progress != syncedReviveProgress) {
            syncedReviveProgress = progress;
            reviveSyncedAtNanos = System.nanoTime();
        }

        long extraTicks = data.isBeingRevived()
                ? (System.nanoTime() - reviveSyncedAtNanos) / 50_000_000L
                : 0L;
        int effective = (int) Math.min((long) total, progress + extraTicks);
        return Math.max(0L, (long) (total - effective) * TICK_MILLIS);
    }

    private static long remainingMillis(PlayerDownedData data) {
        if (data == null) {
            return 0L;
        }

        int ticks = data.getRemainingTicks();
        if (ticks != syncedTicks) {
            syncedTicks = ticks;
            syncedAtNanos = System.nanoTime();
            maxTicks = Math.max(maxTicks, ticks);
        }

        if (data.isBeingRevived()) {
            syncedAtNanos = System.nanoTime();
            return syncedTicks * TICK_MILLIS;
        }

        long elapsed = (System.nanoTime() - syncedAtNanos) / 1_000_000L;
        return Math.max(0L, syncedTicks * TICK_MILLIS - elapsed);
    }

    private static float intensity() {
        if (maxTicks <= 0) {
            return 0.0F;
        }
        return Mth.clamp(1.0F - (float) Math.max(0, syncedTicks) / (float) maxTicks, 0.0F, 1.0F);
    }

    private static int beatIntervalTicks() {
        return (int) Mth.lerp(intensity(), 24.0F, 7.0F);
    }

    private static float pulseAt(long nanos) {
        if (lastBeatNanos == 0L) {
            return 0.0F;
        }
        double seconds = (nanos - lastBeatNanos) / 1_000_000_000.0;
        return (float) Math.exp(-3.2 * seconds);
    }

    private static void beat(float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT, pitch, volume));
        lastBeatNanos = System.nanoTime();
    }

    private static boolean isDowned() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && LastStandDataAttachments.get(mc.player).isDowned();
    }

    private static void reset() {
        syncedTicks = -1;
        syncedAtNanos = 0L;
        syncedReviveProgress = -1;
        reviveSyncedAtNanos = 0L;
        maxTicks = 0;
        lastBeatNanos = 0L;
        beatCountdown = 0;
        dubCountdown = 0;
    }
}
