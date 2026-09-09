package com.minecraft.core.api.collectible.type.companion.type;

import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.list.*;
import lombok.Getter;

@Getter
public enum CompanionType {

    CHIMP(ChimpanzeeCompanion.class, "§6Chimpanzé"),
    MYTHICAL_DRAGON(MythicDragonCompanion.class, "§6Dragão Mítico"),
    PUG(PugCompanion.class, "§6Pug"),
    BOX(BoxCompanion.class, "§6Box"),
    FOX(FoxCompanion.class, "§6Raposa"),
    LION(LionCompanion.class, "§6Leão"),
    PERRY_THE_PLATYPUS(PerryThePlatypusCompanion.class, "§6Perry o Ornitorrinco"),
    GIRAFFE(GiraffeCompanion.class, "§6Girafa"),
    R2_D2(R2D2Companion.class, "§6R2-D2"),
    BB_8(BB8Companion.class, "§6BB-8"),
    FIRE_DRAGON(FireDragonCompanion.class, "§6Dragão de Fogo"),
    PENGUIN(PenguinCompanion.class, "§6Pinguim");

    private static final CompanionType[] VALUES = values();

    private final Class<? extends CompanionCollectible> type;
    private final String defaultName;

    CompanionType(Class<? extends CompanionCollectible> type, String defaultName) {
        this.type = type;
        this.defaultName = defaultName;
    }

    public static void handle() {
        for (CompanionType name : VALUES) {
            try {
                name.type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static CompanionType fromType(Class<? extends CompanionCollectible> type) {
        for (CompanionType name : VALUES) {
            if (name.type.equals(type)) {
                return name;
            }
        }

        return null;
    }
}