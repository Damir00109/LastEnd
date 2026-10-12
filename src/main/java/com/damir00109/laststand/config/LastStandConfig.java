package com.damir00109.laststand.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class LastStandConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> INSTANT_KILL_DAMAGE_TYPES = BUILDER
            .comment(
                    "Damage type ids that kill the player outright instead of putting them into the DOWNED state.",
                    "Lethal damage of these types is never intercepted, so the player dies immediately.",
                    "Examples: \"minecraft:lava\", \"minecraft:drown\", \"minecraft:out_of_world\", \"minecraft:generic_kill\".")
            .defineList("instant_kill_damage_types",
                    List.of(
                            "minecraft:out_of_world",
                            "minecraft:lava",
                            "minecraft:drown",
                            "minecraft:generic_kill"),
                    () -> "",
                    value -> value instanceof String id && ResourceLocation.tryParse(id) != null);

    public static final ModConfigSpec.IntValue REVIVE_SECONDS_MIN = BUILDER
            .comment(
                    "Minimum seconds of bandaging required to revive a downed player.",
                    "The actual revive time is picked randomly between min and max each time a player goes down.",
                    "Progress is kept if reviving is interrupted; the downed player's countdown is frozen while being revived.")
            .defineInRange("revive_seconds_min", 30, 1, 600);

    public static final ModConfigSpec.IntValue REVIVE_SECONDS_MAX = BUILDER
            .comment("Maximum seconds of bandaging required to revive a downed player.")
            .defineInRange("revive_seconds_max", 60, 1, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private LastStandConfig() {
    }

    public static boolean isInstantKill(DamageSource source) {
        String id = source.typeHolder().unwrapKey().map(key -> key.location().toString()).orElse(null);
        return id != null && INSTANT_KILL_DAMAGE_TYPES.get().contains(id);
    }
}
