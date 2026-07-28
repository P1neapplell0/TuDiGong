package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.client.screen.BiomeSearchScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record SyncBiomeDimensionsPacket(Map<ResourceLocation, List<ResourceLocation>> dimensions) implements BasePacket {
    public static final CustomPacketPayload.Type<SyncBiomeDimensionsPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "sync_biome_dimensions"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBiomeDimensionsPacket> STREAM_CODEC = BasePacket.codec(SyncBiomeDimensionsPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeMap(dimensions, FriendlyByteBuf::writeResourceLocation, (byteBuf, list) -> byteBuf.writeCollection(list, FriendlyByteBuf::writeResourceLocation));
    }

    public static SyncBiomeDimensionsPacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncBiomeDimensionsPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, byteBuf -> byteBuf.readList(FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        BiomeSearchScreen.BIOME_DIMENSIONS.clear();
        BiomeSearchScreen.BIOME_DIMENSIONS.putAll(dimensions);
        BiomeSearchScreen.markDataChanged();
    }
}
