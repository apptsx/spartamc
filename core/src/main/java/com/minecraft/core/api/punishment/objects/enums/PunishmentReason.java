package com.minecraft.core.api.punishment.objects.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PunishmentReason {

    CHEATING("Uso de Trapaças"),
    COMMUNITY("Diretrizes da Comunidade");

    private final String name;

    public static PunishmentReason of(String name) {
        return Arrays.stream(values())
                .filter(reason -> reason.name().equalsIgnoreCase(name) || reason.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
