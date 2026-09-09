package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class Caster extends Kit implements Listener {

    public Caster() {
        super("Caster", Item.of(Material.FLINT), KitStyle.STRATEGY,
                Arrays.asList("§7Corte o cooldown dos seus kits", "§7pela metade."));
    }
}
