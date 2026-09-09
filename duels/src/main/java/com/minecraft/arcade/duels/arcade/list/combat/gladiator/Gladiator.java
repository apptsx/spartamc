package com.minecraft.arcade.duels.arcade.list.combat.gladiator;

import com.minecraft.arcade.duels.Duels;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.github.paperspigot.Title;

@IgnoreEvent
public class Gladiator extends Arcade {

    public Gladiator(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_GLADIATOR);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_GLADIATOR);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64(player, base64);

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.IRON);
        } else
            handleDefaultHotbar(player);
    }

    @EventHandler
    public void onGlass(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.hasBlock() && isValid(player)) {
            Block clicked = event.getClickedBlock();

            if (clicked.getType().equals(Material.GLASS)) {
                player.sendBlockChange(clicked.getLocation(), Material.BEDROCK, (byte) 0);
            }
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isValid(player)) {
                Block obsidian = player.getLocation().add(0, 1, 0).getBlock();

                if (obsidian.getType().equals(Material.OBSIDIAN) && !player.hasMetadata("trapped")) {
                    User user = (User) User.of(player.getUniqueId());

                    if (user.inCombat()) {
                        Player suffocate = user.getCombat().getTarget();

                        player.setMetadata("trapped", new FixedMetadataValue(Duels.getPlugin(Duels.class), true));

                        player.sendTitle(new Title(
                                "§4§lSE FODEU", "§4Você foi sufocado por " + suffocate.getName() + "!",
                                0, 60, 50
                        ));

                        suffocate.sendTitle(new Title(
                                "§6§lSUPREMO", "§6Você sufocou " + player.getName() + "!",
                                0, 60, 50
                        ));

                        player.getWorld().strikeLightningEffect(player.getLocation());
                    }
                }
            }
        }
    }
}
