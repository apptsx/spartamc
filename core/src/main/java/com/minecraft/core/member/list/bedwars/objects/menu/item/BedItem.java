package com.minecraft.core.member.list.bedwars.objects.menu.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BedItem {

    private final int slot;
    private final String name;

    private final long timestamp = System.currentTimeMillis();
}
