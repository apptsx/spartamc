package com.minecraft.core.arcade.style;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArcadeStyle {

    NONE("Nenhum"),
    STRATEGY("Estratégia"),
    COMBAT("Combate"),
    FUN("Diversão");

    private final String name;
}
