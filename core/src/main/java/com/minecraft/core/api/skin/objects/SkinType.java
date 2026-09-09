package com.minecraft.core.api.skin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkinType {

    PROFILE("Exclusiva"),
    CUSTOM("Personalizada"),
    LIBRARY("Biblioteca");

    private final String name;
}
