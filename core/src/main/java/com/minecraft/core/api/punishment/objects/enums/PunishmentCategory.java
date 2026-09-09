package com.minecraft.core.api.punishment.objects.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PunishmentCategory {

    BAN("Banimento"),
    MUTE("Mute"),
    SKIN("Skin"),
    REPORT("Report");

    private final String name;

    public static PunishmentCategory of(String name) {
        return Arrays.stream(values()).filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public String getBigName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
