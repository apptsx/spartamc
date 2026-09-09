package com.minecraft.core.backend.database.redis.message.types.route;

import com.minecraft.core.Constant;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class RoomRequestMessage extends RedisMessage {

    private final UUID requester;

    private final ArcadeCategory arcade;
    private final Slot slot;

    private final Type type;
    private final int mapId;

    private final Map<String, Object> properties;

    public RoomRequestMessage(UUID requester, ArcadeCategory arcade, Slot slot, Type type, int mapId) {
        super(Constant.REDIS_ROOM_REQUEST_CHANNEL);

        this.requester = requester;

        this.arcade = arcade;
        this.slot = slot;

        this.type = type;
        this.mapId = mapId;

        this.properties = new HashMap<>();
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key.toLowerCase(), value);
    }
}