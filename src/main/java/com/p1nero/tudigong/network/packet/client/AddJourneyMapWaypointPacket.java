package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.compat.JourneyMapCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record AddJourneyMapWaypointPacket(String name, BlockPos pos, int color) implements BasePacket {
    public static final CustomPacketPayload.Type<AddJourneyMapWaypointPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "add_journeymap_waypoint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddJourneyMapWaypointPacket> STREAM_CODEC = BasePacket.codec(AddJourneyMapWaypointPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
        buf.writeInt(color);
    }

    public static AddJourneyMapWaypointPacket decode(RegistryFriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos blockPos = buf.readBlockPos();
        int color = buf.readInt();
        return new AddJourneyMapWaypointPacket(name, blockPos, color);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            JourneyMapCompat.createNewWaypoint(name, color, pos, Minecraft.getInstance().level.dimension());
        }
    }
}
