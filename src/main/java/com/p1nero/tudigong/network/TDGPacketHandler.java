package com.p1nero.tudigong.network;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.network.packet.client.AddJourneyMapWaypointPacket;
import com.p1nero.tudigong.network.packet.client.AddXaeroMapWaypointPacket;
import com.p1nero.tudigong.network.packet.client.OpenTudiGongDialoguePacket;
import com.p1nero.tudigong.network.packet.client.SyncBiomeDimensionsPacket;
import com.p1nero.tudigong.network.packet.client.SyncHistoryEntryPacket;
import com.p1nero.tudigong.network.packet.client.SyncResourceKeysPacket;
import com.p1nero.tudigong.network.packet.client.SyncStructureDimensionsPacket;
import com.p1nero.tudigong.network.packet.client.SyncStructureSetsPacket;
import com.p1nero.tudigong.network.packet.client.SyncStructureTagsPacket;
import com.p1nero.tudigong.network.packet.client.SyncStructureTypesPacket;
import com.p1nero.tudigong.network.packet.server.HandleNpcInteractionPacket;
import com.p1nero.tudigong.network.packet.server.HandleSearchPacket;
import com.p1nero.tudigong.network.packet.server.TeleportToServerPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class TDGPacketHandler {
    private TDGPacketHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TuDiGongMod.MOD_ID).versioned("2");

        registerClient(registrar, AddXaeroMapWaypointPacket.TYPE, AddXaeroMapWaypointPacket.STREAM_CODEC);
        registerClient(registrar, AddJourneyMapWaypointPacket.TYPE, AddJourneyMapWaypointPacket.STREAM_CODEC);
        registerClient(registrar, SyncResourceKeysPacket.TYPE, SyncResourceKeysPacket.STREAM_CODEC);
        registerClient(registrar, SyncStructureTagsPacket.TYPE, SyncStructureTagsPacket.STREAM_CODEC);
        registerClient(registrar, SyncStructureSetsPacket.TYPE, SyncStructureSetsPacket.STREAM_CODEC);
        registerClient(registrar, SyncStructureDimensionsPacket.TYPE, SyncStructureDimensionsPacket.STREAM_CODEC);
        registerClient(registrar, SyncBiomeDimensionsPacket.TYPE, SyncBiomeDimensionsPacket.STREAM_CODEC);
        registerClient(registrar, SyncHistoryEntryPacket.TYPE, SyncHistoryEntryPacket.STREAM_CODEC);
        registerClient(registrar, SyncStructureTypesPacket.TYPE, SyncStructureTypesPacket.STREAM_CODEC);
        registerClient(registrar, OpenTudiGongDialoguePacket.TYPE, OpenTudiGongDialoguePacket.STREAM_CODEC);

        registerServer(registrar, HandleSearchPacket.TYPE, HandleSearchPacket.STREAM_CODEC);
        registerServer(registrar, TeleportToServerPacket.TYPE, TeleportToServerPacket.STREAM_CODEC);
        registerServer(registrar, HandleNpcInteractionPacket.TYPE, HandleNpcInteractionPacket.STREAM_CODEC);
    }

    private static <T extends BasePacket> void registerClient(PayloadRegistrar registrar,
                                                               CustomPacketPayload.Type<T> type,
                                                               StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec) {
        registrar.playToClient(type, codec, TDGPacketHandler::handle);
    }

    private static <T extends BasePacket> void registerServer(PayloadRegistrar registrar,
                                                               CustomPacketPayload.Type<T> type,
                                                               StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec) {
        registrar.playToServer(type, codec, TDGPacketHandler::handle);
    }

    private static <T extends BasePacket> void handle(T packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.execute(context.player()));
    }

    public static void sendToPlayer(BasePacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(BasePacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToAll(BasePacket packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}
