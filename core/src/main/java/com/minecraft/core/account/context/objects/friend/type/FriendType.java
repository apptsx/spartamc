package com.minecraft.core.account.context.objects.friend.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FriendType {

    DEFAULT("Padrão"),
    BEST("Melhor Amigo");

    private final String name;

    public FriendType next() {
        return this != BEST ? values()[ordinal() + 1] : DEFAULT;
    }
}
