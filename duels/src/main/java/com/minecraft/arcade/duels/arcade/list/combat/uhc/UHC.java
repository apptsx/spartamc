package com.minecraft.arcade.duels.arcade.list.combat.uhc;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

@IgnoreEvent
public class UHC extends Arcade {

    public UHC(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_UHC);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_UHC);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64(player, base64);

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.DIAMOND, Enchantment.PROTECTION_ENVIRONMENTAL);

        } else
            handleDefaultHotbar(player);
    }

    @EventHandler
    public void onHealingApple(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            ItemStack item = player.getItemInHand();

            if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || item.getItemMeta().getDisplayName() == null)
                return;

            if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§6Maça Curadora")) {
                player.addPotionEffects(Arrays.asList(
                        new PotionEffect(PotionEffectType.SPEED, 20 * 10, 0),
                        new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0),
                        new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2)
                ));

                BukkitUtil.consumeItem(player);
            }
        }
    }
}
