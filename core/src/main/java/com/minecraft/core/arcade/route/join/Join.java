package com.minecraft.core.arcade.route.join;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Join {

    PLAYER("Jogador"),
    VANISH("Vanish"),
    SPECTATOR("Espectador");

    private final String name;
}
