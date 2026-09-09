package com.minecraft.core.account.context.objects.tag.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagRole {

    NONE("Nenhuma"),
    COLORED("Colorida"),
    VIP("VIP"),
    SPECIAL("Especial"),
    ONLY_THESE("Somente para"),
    STAFF("Equipe");

    private final String name;
}
