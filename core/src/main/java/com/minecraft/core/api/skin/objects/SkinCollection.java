package com.minecraft.core.api.skin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum SkinCollection {

    NONE("Sem coleção", ChatColor.GRAY),
    DRAGON_BALL("Dragon Ball", ChatColor.GOLD),
    STREET_FIGHTER("Street Fighter", ChatColor.GREEN),
    MORTAL_KOMBAT("Mortal Kombat", ChatColor.RED),
    CHRISTMAS("Natal", ChatColor.RED);

    private final String name;
    private final ChatColor color;

    public String getColoredName() {
        return color + name;
    }
}
