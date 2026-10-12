package com.damir00109.laststand.data;

import com.damir00109.laststand.LastStand;
import com.damir00109.laststand.player.PlayerDownedData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class LastStandDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LastStand.MOD_ID);

    public static final Supplier<AttachmentType<PlayerDownedData>> DOWNED_DATA =
            ATTACHMENT_TYPES.register("downed_data", () -> AttachmentType
                    .builder(() -> new PlayerDownedData())
                    .serialize(PlayerDownedData.CODEC)
                    .copyOnDeath()
                    .sync(PlayerDownedData.STREAM_CODEC)
                    .build());

    public static PlayerDownedData get(Player player) {
        return player.getData(DOWNED_DATA.get());
    }
}
