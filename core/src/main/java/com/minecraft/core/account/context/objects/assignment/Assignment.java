package com.minecraft.core.account.context.objects.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Assignment {

    AUTO("Automaticamente atribuído"),
    STAFF("Atribuído pela equipe"),
    CONSOLE("Atribuído pelo console");

    private final String name;
}
