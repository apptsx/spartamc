package com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopType {

    FINAL_KILL("Kills Finais"),
    BED_DESTRUCTION("Camas Destruídas");

    private final String name;
}
