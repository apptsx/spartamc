package com.minecraft.arcade.bedwars.structure.generator.objects.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneratorLevel {

    NONE("", ""),

    ONE("I", "+50% de recursos"),
    TWO("II", "+100% de recursos"),
    THREE("III", "Gera esmeraldas"),
    FOUR("IV", "+200% de recursos");

    private final String tag;
    private final String description;

    public int getCost() {
        return ordinal() * 2;
    }
}
