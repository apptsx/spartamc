package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class Grandpa extends Kit implements Listener {

    public Grandpa() {
        super("Grandpa", Item.of(Material.STICK), KitStyle.COMBAT,
                Arrays.asList("§7Ganhe um graveto com Repulsão II",
                        "§7, e entre os seus inimigos."));

        setSpecialItems(Item.of(Material.STICK, "§aGrandpa").enchantment(Enchantment.KNOCKBACK, 2));

        setRanks(RankType.VIP);
        setPrice(15000);
    }
}
