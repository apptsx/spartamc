package com.minecraft.core.account.context.objects.joinmessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JoinMessageRarity {

    COMMON("Comum", "§a"),
    EPIC("Épica", "§5"),
    LEGENDARY("Lendária", "§6");

    private final String name;
    private final String color;

    public String getColoredName() {
        return color + name;
    }
}
