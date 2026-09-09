package com.minecraft.core.arcade.style;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TitleStyle {

    NONE(""),
    NEW("§d§lNOVO!"),
    EMPHASIS("§e§lDESTAQUE"),
    MAINTENANCE("§c§lMANUTENÇÃO");

    private final String text;
}
