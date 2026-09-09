package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Phantom extends Kit implements Listener {

    public Phantom() {
        super("Phantom", Item.of(Material.FEATHER), KitStyle.STRATEGY,
                Arrays.asList("§7Voe por 5 segundos, e ganhe",
                        "§7um peito de couro enquanto voa."));

        setSpecialItems(Item.of(Material.FEATHER, "§aPhantom"));

        setCooldown(30);

        setPrice(30000);
        setRanks(RankType.VIP);

        setRestrictedKits(Stomper.class);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClickChestPlate(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (isUsingKit(player) && event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                    && event.getSlot() >= 36 && event.getSlot() <= 39)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && withSpecial(player.getItemInHand()) && event.getAction().name().contains("RIGHT")) {
            if (hasCooldown(player)) return;

            ItemStack chestPlate = player.getInventory().getChestplate();

            /* O jogador já possui um peitoral, e fazer para enviar ao inventário dele */
            if (chestPlate != null && !chestPlate.getType().equals(Material.AIR)) {
                int slot = 0;

                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null || content.getType().equals(Material.AIR) || content.getType().equals(Material.MUSHROOM_SOUP)) {
                        player.getInventory().setItem(slot, chestPlate);
                        break;
                    }

                    slot++;
                }
            }

            player.getInventory().setChestplate(new Item(Material.LEATHER_CHESTPLATE).leatherColor(Color.WHITE));

            player.setAllowFlight(true);
            player.setFlying(true);

            player.sendMessage("§aAgora você pode voar por 5 segundos.");
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

            player.teleport(player.getLocation().clone().add(1.5, 1.5, 1.5));

            new BukkitRunnable() {
                int time = 5;

                @Override
                public void run() {
                    if (!player.isOnline() || !isAllow(player)) {
                        cancel();
                        return;
                    }

                    time--;

                    if (time <= 3 && time >= 1) {
                        player.sendMessage("§c" + time + "...");

                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    }

                    if (time == 0) {
                        player.getInventory().setChestplate(new Item(Material.AIR));

                        player.setAllowFlight(false);
                        player.setFlying(false);

                        player.sendMessage("§cSeu tempo de voo chegou ao fim.");
                        player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1.0f, 1.0f);

                        cancel();
                    }
                }
            }.runTaskTimer(PvP.getInstance(), 0, 20);

            applyCooldown(player);
        }
    }
}
