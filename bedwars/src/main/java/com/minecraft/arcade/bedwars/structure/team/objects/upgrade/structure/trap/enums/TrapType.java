package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums;

import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.action.TrapAction;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum TrapType {

    BLACKOUT("Apagão", Material.EYE_OF_ENDER, BedOre.DIAMOND, 1, Arrays.asList(
            "§7Aplique os efeitos Cegueira",
            "§7e Lentidão por 8 segundos."
    ), (executor, team) -> {
        executor.addPotionEffects(Arrays.asList(
                new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 0),
                new PotionEffect(PotionEffectType.SLOW, 20 * 8, 0)
        ));
    }),

    FATIGUE("Fadiga", Material.GOLD_PICKAXE, BedOre.DIAMOND, 1, Collections.singletonList(
            "§7Cause fadiga nos invasores."
    ), ((executor, team) -> executor.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 5, 0)))),

    CURSE("Maldição", Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ4ZWY1Yjg2MGU0N2Q2NmY3ZTAwZjZjOWY3ZjViZGM5MzVlZGJjZTAwYWViNzU2ZjQxNTk5OWFkZTE2ZTM2YyJ9fX0="),
            BedOre.DIAMOND, 3, Arrays.asList(
            "§7Quem pisar na sua ilha,",
            "§7será amaldiçoado."
    ), ((executor, team) -> executor.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 5, 1))));

    private final String name;

    private final Item icon;
    private final BedOre ore;

    private final int cost;
    private final List<String> description;

    private final TrapAction action;

    TrapType(String name, Material icon, BedOre ore, int cost, List<String> description, TrapAction action) {
        this(name, Item.of(icon), ore, cost, description, action);
    }

    public static TrapType of(String name) {
        return Stream.of(values())
                .filter(trap -> trap.name().equalsIgnoreCase(name) || trap.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean canPurchase(Player player) {
        return BukkitUtil.getItemAmount(player, ore.getMaterial()) >= cost;
    }
}
