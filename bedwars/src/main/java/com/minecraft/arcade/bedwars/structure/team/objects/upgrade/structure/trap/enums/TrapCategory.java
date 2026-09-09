package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums;

public enum TrapCategory {
    PRIMARY, SECONDARY, THIRD;

    public static TrapCategory of(int id) {
        return values()[id];
    }
}
