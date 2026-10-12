package com.damir00109.laststand.downed;

import com.mojang.serialization.Codec;

public enum DownedState {
    NORMAL,
    DOWNED;

    public static final Codec<DownedState> CODEC = Codec.STRING.xmap(DownedState::valueOf, DownedState::name);
}
