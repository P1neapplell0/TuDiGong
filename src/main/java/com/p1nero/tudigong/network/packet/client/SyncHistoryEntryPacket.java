package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.client.util.SearchHistoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Packet sent from the server to the client to add a new entry to the search history.
 */
public record SyncHistoryEntryPacket(String searchTerm, Component dialogueType, @Nullable BlockPos position, @Nullable ResourceKey<Level> dimension) implements BasePacket {
    public static final CustomPacketPayload.Type<SyncHistoryEntryPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "sync_history_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHistoryEntryPacket> STREAM_CODEC = BasePacket.codec(SyncHistoryEntryPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.searchTerm);
        ComponentSerialization.STREAM_CODEC.encode(buf, this.dialogueType);
        buf.writeNullable(this.position, (buffer, pos) -> buffer.writeBlockPos(pos));
        buf.writeNullable(this.dimension, (b, k) -> b.writeResourceKey(k));
    }

    public static SyncHistoryEntryPacket decode(RegistryFriendlyByteBuf buf) {
        String searchTerm = buf.readUtf();
        Component type = ComponentSerialization.STREAM_CODEC.decode(buf);
        BlockPos position = buf.readNullable(buffer -> buffer.readBlockPos());
        ResourceKey<Level> dimension = buf.readNullable(b -> b.readResourceKey(Registries.DIMENSION));
        return new SyncHistoryEntryPacket(searchTerm, type, position, dimension);
    }

    /**
     * Executed on the client side to add the received entry to the history manager.
     */
    @Override
    public void execute(@Nullable Player player) {
        SearchHistoryManager.addEntry(
                new SearchHistoryManager.SearchHistoryEntry(
                        this.searchTerm,
                        this.dialogueType,
                        this.position,
                        this.dimension,
                        System.currentTimeMillis()
                )
        );
    }
}
