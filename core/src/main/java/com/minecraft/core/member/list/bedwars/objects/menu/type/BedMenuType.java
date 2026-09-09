package com.minecraft.core.member.list.bedwars.objects.menu.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum BedMenuType {
    FAVORITE, ITEM_BAR;

    public static List<BedMenuType> list() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
