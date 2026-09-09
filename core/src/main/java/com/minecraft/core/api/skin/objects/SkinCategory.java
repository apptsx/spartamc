package com.minecraft.core.api.skin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkinCategory {

    NONE("Nenhum"),
    DIVERSAS("Diversas"),
    SUPER_HEROES("Super Heróis"),
    JOGOS("Jogos"),
    HALLOWEEN("Halloween"),
    NATALINA("Natalinas");

    private final String name;

    public SkinCategory next() {
        return this != NATALINA ? values()[ordinal() + 1] : NONE;
    }
}
