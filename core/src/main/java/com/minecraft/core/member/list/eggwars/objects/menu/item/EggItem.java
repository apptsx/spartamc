package com.minecraft.core.member.list.eggwars.objects.menu.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EggItem {

    private final int slot;
    private final String name;

    private final long timestamp = System.currentTimeMillis();
}

