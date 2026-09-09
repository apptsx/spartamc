package com.minecraft.core.api.skin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkinGender {

    MAN("Masculino"),
    FEMALE("Feminino");

    private final String name;
}
