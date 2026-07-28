package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.client.screen.BiomeSearchScreen;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.util.BiomeUtil;
import com.p1nero.tudigong.util.StructureUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record SyncResourceKeysPacket(List<ResourceLocation> resourceLocations, boolean isStructure) implements BasePacket {
    public static final CustomPacketPayload.Type<SyncResourceKeysPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "sync_resource_keys"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResourceKeysPacket> STREAM_CODEC = BasePacket.codec(SyncResourceKeysPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isStructure);
        buf.writeInt(resourceLocations.size());
        resourceLocations.forEach(buf::writeResourceLocation);
    }

    public static SyncResourceKeysPacket decode(RegistryFriendlyByteBuf buf) {
        boolean isStructure = buf.readBoolean();
        int size = buf.readInt();
        List<ResourceLocation> newResourceLocations = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            newResourceLocations.add(buf.readResourceLocation());
        }
        return new SyncResourceKeysPacket(newResourceLocations, isStructure);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if(isStructure) {
            StructureSearchScreen.STRUCTURE_NAME_MAP.clear();
            StructureSearchScreen.STRUCTURE_MOD_IDS.clear();
            resourceLocations.forEach((resourceLocation -> {
                StructureSearchScreen.STRUCTURE_NAME_MAP.put(resourceLocation, StructureUtils.getPrettyStructureName(resourceLocation));
                String modId = resourceLocation.getNamespace().toLowerCase(Locale.ROOT);
                StructureSearchScreen.STRUCTURE_MOD_IDS.computeIfAbsent(modId, k -> new java.util.HashSet<>()).add(resourceLocation);
            }));
            StructureSearchScreen.markDataChanged();
        } else {
            BiomeSearchScreen.BIOME_NAME_MAP.clear();
            BiomeSearchScreen.BIOME_MOD_IDS.clear();
            resourceLocations.forEach((resourceLocation -> {
                BiomeSearchScreen.BIOME_NAME_MAP.put(resourceLocation, BiomeUtil.getBiomeName(resourceLocation));
                String modId = resourceLocation.getNamespace().toLowerCase(Locale.ROOT);
                BiomeSearchScreen.BIOME_MOD_IDS.computeIfAbsent(modId, k -> new java.util.HashSet<>()).add(resourceLocation);
            }));
            BiomeSearchScreen.markDataChanged();
        }
    }
}
