package com.p1nero.tudigong.network.packet.server;

import com.p1nero.tudigong.entity.TudiGongEntity;
import com.p1nero.tudigong.network.BasePacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record HandleNpcInteractionPacket(int entityId, int interactionId) implements BasePacket {
    public static final CustomPacketPayload.Type<HandleNpcInteractionPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "handle_npc_interaction"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HandleNpcInteractionPacket> STREAM_CODEC = BasePacket.codec(HandleNpcInteractionPacket::decode);

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeVarInt(this.interactionId);
    }

    private static HandleNpcInteractionPacket decode(RegistryFriendlyByteBuf buffer) {
        return new HandleNpcInteractionPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Entity entity = serverPlayer.level().getEntity(this.entityId);
        if (entity instanceof TudiGongEntity tudiGong) {
            tudiGong.handleNpcInteraction(serverPlayer, this.interactionId);
            if (this.interactionId == 0) {
                tudiGong.setConversingPlayer(null);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
