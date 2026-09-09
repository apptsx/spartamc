package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.payload.RoomPayload;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ReconnectData {

    private final String RECONNECT_KEY = "reconnect:";

    private final RedisDatabase redis;

    public void save(Reconnect reconnect) {
        redis.save(RECONNECT_KEY + reconnect.getSender(), reconnect, 60 * 3);
    }

    public Reconnect of(String arenaId, String teamId) {
        return list().stream().filter(reconnect -> {
            ArcadeRouteContext route = reconnect.getRoute();

            return route.getArenaIdentifier() != null && route.getArenaIdentifier().equalsIgnoreCase(arenaId)
                    && route.getTeamId() != null && route.getTeamId().equalsIgnoreCase(teamId);
        }).findFirst().orElse(null);
    }

    public Reconnect of(UUID sender) {
        /* Busca a reconexão baseando na rota, e verificando se a sala continua ativa */

        Reconnect reconnect = redis.load(RECONNECT_KEY + sender, Reconnect.class);

        if (reconnect == null) return null;

        ArcadeRouteContext route = reconnect.getRoute();

        ArcadePayload payload = Core.getArcadeData().read(route.getArcade(), reconnect.getRoute().getServerId());

        if (payload == null) {
            delete(sender);
            return null;
        }

        RoomPayload room = payload.getRoom(route);

        if (room == null || !room.getPhase().equals(RoomPhase.PLAYING)) {
            delete(sender);
            return null;
        }

        return reconnect;
    }

    public void delete(UUID sender) {
        redis.delete(RECONNECT_KEY + sender);
    }

    public List<Reconnect> list() {
        return redis.loadAll(RECONNECT_KEY, Reconnect.class);
    }

}
