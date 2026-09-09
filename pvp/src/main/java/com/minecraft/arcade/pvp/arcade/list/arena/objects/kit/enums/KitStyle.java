package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KitStyle {

    NONE("Nenhum"),
    STRATEGY("Estratégia"),
    COMBAT("Combate"),
    MOVEMENT("Movimentação");

    private final String name;
}