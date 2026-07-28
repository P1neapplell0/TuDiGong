package com.p1nero.tudigong.network.packet.client;

import com.p1nero.tudigong.client.screen.TudiGongDialogueScreen;
import com.p1nero.tudigong.network.BasePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record OpenTudiGongDialoguePacket(int entityId, boolean fromHurt) implements BasePacket {
    public static final CustomPacketPayload.Type<OpenTudiGongDialoguePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tudigong", "open_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTudiGongDialoguePacket> STREAM_CODEC = BasePacket.codec(OpenTudiGongDialoguePacket::decode);

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeBoolean(this.fromHurt);
    }

    private static OpenTudiGongDialoguePacket decode(RegistryFriendlyByteBuf buffer) {
        return new OpenTudiGongDialoguePacket(buffer.readVarInt(), buffer.readBoolean());
    }

    @Override
    public void execute(@Nullable Player player) {
        Minecraft.getInstance().setScreen(new TudiGongDialogueScreen(this.entityId, this.fromHurt));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
