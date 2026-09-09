package com.minecraft.core.backend.database.redis.message.types.route;

import com.minecraft.core.Constant;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RoomInfoMessage extends RedisMessage {

    private final UUID requester;
    private final InfoType info;

    private final String code;

    private final ArcadeCategory arcade;
    private final Slot slot;

    private final Type mode;
    private final int mapId;

    public RoomInfoMessage(UUID requester, InfoType info, String code, ArcadeCategory arcade, Slot slot, Type mode, int mapId) {
        super(Constant.REDIS_ROOM_INFO_CHANNEL);

        this.requester = requester;
        this.info = info;

        this.code = code;

        this.arcade = arcade;
        this.slot = slot;

        this.mode = mode;
        this.mapId = mapId;
    }

    public boolean isValid() {
        return arcade != null;
    }

    @Getter
    @AllArgsConstructor
    public enum InfoType {

        DONE("§aSala criada!"),
        SENDING_TO("§aEnviando para %s..."),
        GAME_NOT_FOUND("§cO modo de jogo solicitado não foi encontrado."),
        UNKNOWN_ERROR("§cOcorreu um erro inesperado ao solicitar a sala!");

        private final String message;
    }
}