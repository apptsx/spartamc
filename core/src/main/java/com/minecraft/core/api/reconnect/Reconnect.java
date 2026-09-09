package com.minecraft.core.api.reconnect;

import com.minecraft.core.arcade.route.ArcadeRouteContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Reconnect {

    private final UUID sender;
    private final ArcadeRouteContext route;

    public String getTeamId() {
        return route.getTeamId();
    }
}
