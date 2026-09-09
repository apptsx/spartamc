package com.minecraft.core.account.context.objects.route;

import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.server.type.ServerType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class RouteContext {

    private UUID senderId;
    private int serverId, serverPort;

    private ServerType serverType, lastServer;

    private ArcadeRouteContext arcade;

    private long updatedAt;

    public static RouteContext bungee() {
        return RouteContext.builder()
                .senderId(UUID.randomUUID())
                .serverId(0)
                .serverPort(25565)
                .serverType(ServerType.BUNGEE)
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    public static RouteContext copy(UUID sender, RouteContext context) {
        return RouteContext.builder()
                .senderId(sender)
                .serverType(context.getServerType())
                .serverId(context.getServerId())
                .serverPort(context.getServerPort())
                .arcade(context.getArcade())
                .updatedAt(context.getUpdatedAt())
                .build();
    }

    public boolean isValidArcade() {
        return arcade != null && arcade.getArcade() != null;
    }
}
