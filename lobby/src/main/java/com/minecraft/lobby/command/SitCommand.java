package com.minecraft.lobby.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.listener.SitListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class SitCommand {

    // Plugin instance para agendar tarefas
    private static org.bukkit.plugin.Plugin getPlugin() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("Lobby");
    }

    @Command(name = "sit", rank = com.minecraft.core.account.context.objects.rank.type.RankType.VIP, onlyPlayer = true)
    public void sit(BukkitCommandContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        
        if (player.getVehicle() != null) {
            player.sendMessage("§cVocê já está sentado!");
            return;
        }
        
        Location seatLoc = loc.clone().add(0, -0.5, 0);
        ArmorStand seat = loc.getWorld().spawn(seatLoc, ArmorStand.class);
        seat.setVisible(false);
        seat.setSmall(true);
        seat.setMarker(true);
        seat.setGravity(false);
        
        seat.setPassenger(player);
        
        SitListener.startBodySync(player, seat);
        
        player.sendMessage("§aVocê sentou!");
        
        Bukkit.getScheduler().runTaskLater(
            getPlugin(),
            () -> {
                if (!seat.isDead() && seat.getPassenger() != null) {
                    seat.remove();
                    player.sendMessage("§eVocê ficou sentado por muito tempo!");
                }
            },
            6000L
        );
    }
    
    @Command(name = "stand", rank = com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER, onlyPlayer = true)
    public void stand(BukkitCommandContext context) {
        Player player = context.getPlayer();
        
        if (player.getVehicle() instanceof ArmorStand) {
            ArmorStand seat = (ArmorStand) player.getVehicle();
            if (!seat.isDead()) {
                seat.remove();
                player.sendMessage("§aVocê levantou!");
            }
        } else {
            player.sendMessage("§cVocê não está sentado!");
        }
    }
}
