package com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileType {

    NORMAL("Casual"),
    RANKED("Competitivo");

    private final String name;
}
