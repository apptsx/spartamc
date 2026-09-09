package com.minecraft.core.arcade.room.team.preset;

import lombok.AllArgsConstructor;
import lombok.Getter;
//import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TeamPresetType {

    RED("Vermelho", "V", ChatColor.RED, Color.RED),
    BLUE("Azul", "A", ChatColor.BLUE, Color.BLUE),
    YEL("Amarelo", "A", ChatColor.YELLOW, Color.YELLOW),
    ORAN("Laranja", "L", ChatColor.GOLD, Color.ORANGE),
    PURP("Roxo", "R", ChatColor.DARK_PURPLE, Color.PURPLE),
    CYAN("Ciano", "C", ChatColor.AQUA, Color.AQUA),
    WHIT("Branco", "B", ChatColor.WHITE, Color.WHITE),
    GRAY("Cinza", "C", ChatColor.DARK_GRAY, Color.GRAY);

    private final String name;
    private final String id;

    private final ChatColor color;
    private final Color rgb;

    public static TeamPresetType of(ChatColor color) {
        return Arrays.stream(values())
                .filter(team -> team.getColor().equals(color) || team.getRgb().toString().equalsIgnoreCase(color.toString()))
                .findFirst()
                .orElse(null);
    }
}
