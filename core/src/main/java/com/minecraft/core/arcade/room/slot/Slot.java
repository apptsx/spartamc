package com.minecraft.core.arcade.room.slot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Slot {

    NONE("Nenhum", "0v0", 0),
    SOLO("Solo", "1v1", 2),
    DUO("Duplas", "2v2", 4),
    TRIO("Trio", "3v3", 6),
    QUARTET("Quartet", "4v4", 8);

    private final String name, id;
    private final int maxPlayers;

    public String getFullName() {
        return name + " (" + id + ")";
    }
}
