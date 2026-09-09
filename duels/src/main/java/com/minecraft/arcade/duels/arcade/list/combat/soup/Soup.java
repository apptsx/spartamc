package com.minecraft.arcade.duels.arcade.list.combat.soup;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

@IgnoreEvent
public class Soup extends Arcade {

    public Soup(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_SOUP);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            PlayerInventory inv = player.getInventory();

            inv.setItem(0, Item.of(Material.DIAMOND_SWORD)
                    .unbreakable()
                    .enchantment(Enchantment.DAMAGE_ALL, 1));

            for (int i = 1; i < 9; i++)
                inv.setItem(i, Item.of(Material.MUSHROOM_SOUP));

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.IRON);
        } else
            handleDefaultHotbar(player);
    }
}
