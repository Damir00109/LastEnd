package com.damir00109.laststand.client;

import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.item.BandageItem;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = LastStand.MOD_ID, value = Dist.CLIENT)
public final class ReviveHudRenderer {
    private static final long TICK_MILLIS = 50L;

    private static int syncedProgress = -1;
    private static long syncedAtNanos;

    private ReviveHudRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isUsingItem()
                || !(mc.player.getUseItem().getItem() instanceof BandageItem)) {
            syncedProgress = -1;
            return;
        }

        LivingEntity target = BandageItem.findDownedTarget(mc.player);
        if (!(target instanceof Player downed)) {
            syncedProgress = -1;
            return;
        }

        PlayerDownedData data = LastStandDataAttachments.get(downed);
        if (!data.isDowned() || data.getReviveTotal() <= 0) {
            syncedProgress = -1;
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = graphics.guiWidth() / 2;
        int textY = graphics.guiHeight() - 52;

        String time = String.format("%.2f", remainingMillis(data) / 1000.0);
        String label = Component.translatable("gui.laststand.reviving", downed.getName().getString(), time).getString();

        graphics.drawString(mc.font, label, centerX - mc.font.width(label) / 2, textY, 0xFFFFFFFF, true);
    }

    private static long remainingMillis(PlayerDownedData data) {
        int progress = data.getReviveProgress();
        if (progress != syncedProgress) {
            syncedProgress = progress;
            syncedAtNanos = System.nanoTime();
        }

        long extraTicks = (System.nanoTime() - syncedAtNanos) / 50_000_000L;
        int effective = (int) Math.min((long) data.getReviveTotal(), progress + extraTicks);
        return Math.max(0L, (long) (data.getReviveTotal() - effective) * TICK_MILLIS);
    }
}
