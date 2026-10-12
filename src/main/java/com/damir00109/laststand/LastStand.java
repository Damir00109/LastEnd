package com.damir00109.laststand;

import com.mojang.logging.LogUtils;
import com.damir00109.laststand.command.LastStandCommand;
import com.damir00109.laststand.config.LastStandConfig;
import com.damir00109.laststand.data.LastStandDataAttachments;
import com.damir00109.laststand.event.MobTargetHandler;
import com.damir00109.laststand.event.PlayerActionHandler;
import com.damir00109.laststand.event.PlayerDamageHandler;
import com.damir00109.laststand.event.PlayerRespawnHandler;
import com.damir00109.laststand.event.PlayerTickHandler;
import com.damir00109.laststand.item.LastStandItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(LastStand.MOD_ID)
public class LastStand {
    public static final String MOD_ID = "laststand";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LastStand(IEventBus modEventBus, ModContainer modContainer) {
        LastStandDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        LastStandItems.ITEMS.register(modEventBus);
        modEventBus.addListener(LastStandItems::onBuildCreativeTabContents);

        modContainer.registerConfig(ModConfig.Type.COMMON, LastStandConfig.SPEC);

        NeoForge.EVENT_BUS.register(PlayerDamageHandler.class);
        NeoForge.EVENT_BUS.register(PlayerTickHandler.class);
        NeoForge.EVENT_BUS.register(PlayerRespawnHandler.class);
        NeoForge.EVENT_BUS.register(PlayerActionHandler.class);
        NeoForge.EVENT_BUS.register(MobTargetHandler.class);
        NeoForge.EVENT_BUS.addListener(LastStand::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        LastStandCommand.register(event.getDispatcher());
    }
}
