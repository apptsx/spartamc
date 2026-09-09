package com.minecraft.core.api.party.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PartyType {

    PUBLIC("Pública"),
    PRIVATE("Privada");

    private final String name;

    public static PartyType of(String name) {
        return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name)).findFirst().orElse(PUBLIC);
    }
}
