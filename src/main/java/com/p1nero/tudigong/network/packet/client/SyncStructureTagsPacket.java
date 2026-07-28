package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncStructureTagsPacket(Map<String, Set<ResourceLocation>> tags) implements BasePacket {
    public static final CustomPacketPayload.Type<SyncStructureTagsPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "sync_structure_tags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStructureTagsPacket> STREAM_CODEC = BasePacket.codec(SyncStructureTagsPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeMap(tags, FriendlyByteBuf::writeUtf, (byteBuf, resourceLocations) -> byteBuf.writeCollection(resourceLocations, FriendlyByteBuf::writeResourceLocation));
    }

    public static SyncStructureTagsPacket decode(RegistryFriendlyByteBuf buf) {
        Map<String, Set<ResourceLocation>> tags = buf.readMap(FriendlyByteBuf::readUtf, byteBuf -> byteBuf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        return new SyncStructureTagsPacket(tags);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        StructureSearchScreen.STRUCTURE_TAGS.clear();
        StructureSearchScreen.STRUCTURE_TAGS.putAll(tags);
        StructureSearchScreen.markDataChanged();
    }
}
