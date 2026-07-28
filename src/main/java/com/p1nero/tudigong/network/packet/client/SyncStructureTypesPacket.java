package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.network.BasePacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SyncStructureTypesPacket implements BasePacket {
    public static final CustomPacketPayload.Type<SyncStructureTypesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "sync_structure_types"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStructureTypesPacket> STREAM_CODEC = BasePacket.codec(SyncStructureTypesPacket::decode);

    private final Map<String, Set<ResourceLocation>> types;
    private final Map<ResourceLocation, ResourceLocation> structureToTypeMap;

    public SyncStructureTypesPacket(Map<ResourceLocation, Collection<ResourceLocation>> types,
                                    Map<ResourceLocation, ResourceLocation> structureToTypeMap) {
        this.types = new HashMap<>();
        types.forEach((key, value) -> this.types.put(key.toString(), new HashSet<>(value)));
        this.structureToTypeMap = new HashMap<>(structureToTypeMap);
    }

    private SyncStructureTypesPacket(RegistryFriendlyByteBuf buffer) {
        this.types = buffer.readMap(FriendlyByteBuf::readUtf,
                valueBuffer -> valueBuffer.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        this.structureToTypeMap = buffer.readMap(FriendlyByteBuf::readResourceLocation,
                FriendlyByteBuf::readResourceLocation);
    }

    private static SyncStructureTypesPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SyncStructureTypesPacket(buffer);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeMap(this.types, FriendlyByteBuf::writeUtf,
                (valueBuffer, values) -> valueBuffer.writeCollection(values, FriendlyByteBuf::writeResourceLocation));
        buffer.writeMap(this.structureToTypeMap, FriendlyByteBuf::writeResourceLocation,
                FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public void execute(@Nullable Player player) {
        StructureSearchScreen.STRUCTURE_TYPES.clear();
        StructureSearchScreen.STRUCTURE_TYPES.putAll(this.types);
        StructureSearchScreen.STRUCTURE_TO_TYPE_MAP.clear();
        StructureSearchScreen.STRUCTURE_TO_TYPE_MAP.putAll(this.structureToTypeMap);
        StructureSearchScreen.markDataChanged();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
