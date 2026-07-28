package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.network.BasePacket;
import com.p1nero.tudigong.compat.XaeroMapCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record AddXaeroMapWaypointPacket(String name, BlockPos pos, @Nullable String color) implements BasePacket {
    public static final CustomPacketPayload.Type<AddXaeroMapWaypointPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "add_xaero_waypoint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddXaeroMapWaypointPacket> STREAM_CODEC = BasePacket.codec(AddXaeroMapWaypointPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
        buf.writeUtf(Objects.requireNonNullElse(color, "null"));
    }

    public static AddXaeroMapWaypointPacket decode(RegistryFriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos blockPos = buf.readBlockPos();
        String color = buf.readUtf();
        return new AddXaeroMapWaypointPacket(name, blockPos, color);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            XaeroMapCompat.createWaypoint(pos, name, color);
        }
    }
}
