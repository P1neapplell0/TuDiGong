package com.p1nero.tudigong.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface BasePacket extends CustomPacketPayload {
    void encode(RegistryFriendlyByteBuf buffer);

    void execute(@Nullable Player player);

    static <T extends BasePacket> StreamCodec<RegistryFriendlyByteBuf, T> codec(Function<RegistryFriendlyByteBuf, T> decoder) {
        return StreamCodec.ofMember(BasePacket::encode, buffer -> decoder.apply(buffer));
    }
}
