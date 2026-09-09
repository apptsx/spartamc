package com.minecraft.core.arcade.room.payload;

import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.type.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPayload {

    private final int id;
    private final String identifier;

    private final Map map;
    private final Type type;

    private RoomPhase phase;

    private int onlinePlayers;

    public RoomPayload(Room room) {
        this.id = room.getId();
        this.identifier = room.getIdentifier();

        this.map = room.getMap();
        this.type = room.getType();

        this.phase = room.getPhase();
    }
}