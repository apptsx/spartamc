package com.minecraft.core.arcade.payload;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.payload.RoomPayload;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class ArcadePayload {

    private final int serverId;
    private final ArcadeCategory category;

    private final List<Map> maps;
    private List<RoomPayload> rooms;

    public ArcadePayload(int serverId, ArcadeCategory category, List<Map> maps, List<RoomPayload> rooms) {
        this.serverId = serverId;
        this.category = category;

        this.maps = maps;
        this.rooms = rooms;
    }

    private int onlinePlayers;

    public RoomPayload getRoom(ArcadeRouteContext route) {
        return getRooms(room -> room.getId() == route.getRoomId() && room.getMap().getId() == route.getMapId())
                .stream().findFirst().orElse(null);
    }

    public List<RoomPayload> getRooms(Predicate<RoomPayload> filter) {
        return rooms.stream().filter(filter).collect(Collectors.toList());
    }

    public List<RoomPayload> getRooms(Map map) {
        return getRooms(payload -> payload.getMap().equals(map));
    }

    public List<RoomPayload> getRooms(Type type) {
        return getRooms(payload -> payload.getType().equals(type));
    }

    public int getOnlinePlayers(Type type) {
        int count = 0;

        for (RoomPayload room : getRooms(type)) {
            count += room.getOnlinePlayers();
        }

        return count;
    }
}
