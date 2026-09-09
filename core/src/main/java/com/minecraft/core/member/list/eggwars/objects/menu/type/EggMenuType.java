package com.minecraft.core.member.list.eggwars.objects.menu.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EggMenuType {
    FAVORITE, ITEM_BAR;

    public static List<EggMenuType> list() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}

