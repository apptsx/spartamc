package com.minecraft.lobby.parkour;

import com.minecraft.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ParkourCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§e=== PARKOUR ===");
            player.sendMessage("§7Use §f/parkour leave §7para sair do parkour");
            player.sendMessage("§7Use §f/parkour status §7para ver seu progresso");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("leave")) {
            if (ParkourManager.isInParkour(player)) {
                ParkourManager.removePlayer(player);
                player.sendMessage("§cVocê saiu do parkour!");
            } else {
                player.sendMessage("§cVocê não está no parkour!");
            }
            return true;
        }
        
        if (subCommand.equals("status")) {
            if (ParkourManager.isInParkour(player)) {
                int checkpoint = ParkourManager.getCurrentCheckpoint(player);
                player.sendMessage("§a§lPROGRESSO DO PARKOUR");
                player.sendMessage("§7Checkpoint atual: §e" + checkpoint + "/4");
                
                if (checkpoint > 0) {
                    ParkourData data = ParkourManager.getPlayerData(player);
                    if (data != null) {
                        ParkourCourse course = ParkourManager.getCourseByName(data.getCourseName());
                        if (course != null && data.getCurrentCheckpoint() > 0) {
                            Location loc = course.getCheckpoint(data.getCurrentCheckpoint());
                            player.sendMessage("§7Último checkpoint: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        }
                    }
                }
            } else {
                player.sendMessage("§cVocê não está no parkour! Toque na placa dourada no início para começar.");
            }
            return true;
        }
        
        return false;
    }
}
