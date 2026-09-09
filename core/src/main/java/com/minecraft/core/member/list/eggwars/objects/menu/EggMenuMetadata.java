package com.minecraft.core.member.list.eggwars.objects.menu;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.list.eggwars.objects.menu.item.EggItem;
import com.minecraft.core.member.list.eggwars.objects.menu.type.EggMenuType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EggMenuMetadata extends MenuMetadata {

    private final EggMenuType type;

    private final List<EggItem> itemList = new ArrayList<>();

    public EggMenuMetadata(EggMenuType type) {
        super(ArcadeCategory.NONE, "...");

        this.type = type;
    }

    public boolean hasItem(String name) {
        return itemList.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
    }
}

