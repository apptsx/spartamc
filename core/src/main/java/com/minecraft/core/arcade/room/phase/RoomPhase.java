package com.minecraft.core.arcade.room.phase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomPhase {

    WAITING("Aguardando"),
    STARTING("Iniciando"),
    RESTARTING("Reiniciando"),
    PLAYING("Jogando"),
    ENDING("Acabando"),
    RESETTING("Restaurando");

    private final String name;
}
