package com.minecraft.core.account.context.objects.rank.type.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankRole {

    NONE("Nenhuma"),
    VIP("VIP"),
    SPECIAL("Especial"),
    STAFF("Equipe");

    private final String name;
}
