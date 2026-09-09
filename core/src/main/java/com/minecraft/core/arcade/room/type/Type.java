package com.minecraft.core.arcade.room.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Type {

    CASUAL("Casual"),
    COMPETITIVE("Competitivo"),
    CUSTOM("Customizada");

    private final String name;
}
