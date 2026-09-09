package com.minecraft.core.member.list.bedwars.objects.ability.enums;

import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum AbilityType {

    QUICK_RESPAWN(
            "Renascimento rápido",
            new Item(Material.WATCH),
            150,
            Arrays.asList(
                    "§7Reduza seu tempo de renascimento",
                    "§7de §c5s §7para §a3s§7."),
            Sound.NOTE_PLING
    ),

    PERMANENT_SWORD(
            "Espada permanente",
            new Item(Material.DIAMOND_SWORD),
            200,
            Arrays.asList(
                    "§7Mantenha sua espada mesmo",
                    "§7ao morrer durante a partida."),
            Sound.ITEM_PICKUP
    ),

    NO_FALL(
            "Queda nula",
            new Item(Material.FEATHER),
            125,
            Arrays.asList(
                    "§7Não receba dano de queda",
                    "§7durante 2 minutos de partida."),
            Sound.FIZZ
    );

    private final String name;
    private final Item icon;
    private final int cost;
    private final List<String> description;
    private final Sound purchaseSound;

    public String getDisplayName() {
        return "§a" + name;
    }
    public String getCostFormatted() {
        return "§e" + cost + " moedas";
    }
}

