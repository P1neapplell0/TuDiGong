package com.p1nero.tudigong.compat;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.AddJourneyMapWaypointPacket;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Locale;

public final class JourneyMapWaypointHelper {
    private JourneyMapWaypointHelper() {
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos) {
        RandomSource random = player.getRandom();
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        TDGPacketHandler.sendToPlayer(new AddJourneyMapWaypointPacket(key, pos, color.getRGB()), player);
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, int color) {
        TDGPacketHandler.sendToPlayer(new AddJourneyMapWaypointPacket(key, pos, color), player);
    }

    public static void createNewWaypoint(String name, int color, BlockPos pos, ResourceKey<Level> dimension) {
        try {
            Class<?> factoryClass = Class.forName("journeymap.api.client.waypoint.ClientWaypointFactoryImpl");
            Method create = factoryClass.getMethod("createWaypoint", String.class, BlockPos.class, String.class,
                    String.class, boolean.class, boolean.class, int.class, boolean.class);
            Object waypoint = create.invoke(null, TuDiGongMod.MOD_ID, pos, TextUtil.tryToGetName(name),
                    dimension.location().toString(), true, true, color, true);

            Class<?> apiClass = Class.forName("journeymap.api.client.impl.ClientAPI");
            Object api = apiClass.getField("INSTANCE").get(null);
            Class<?> waypointType = Class.forName("journeymap.api.v2.common.waypoint.Waypoint");
            apiClass.getMethod("addWaypoint", String.class, waypointType)
                    .invoke(api, name.toLowerCase(Locale.ROOT), waypoint);
        } catch (ReflectiveOperationException exception) {
            TuDiGongMod.LOGGER.error("Unable to add JourneyMap waypoint", exception);
        }
    }
}
