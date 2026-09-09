package com.minecraft.lobby.parkour;

import com.minecraft.lobby.Lobby;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ParkourListener implements Listener {

    private static final Map<Player, Long> lastMessageTime = new HashMap<Player, Long>();
    private static final Map<Player, String> lastMessage = new HashMap<Player, String>();

    public ParkourListener() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.hasBlock()) {
            ItemStack item = event.getItem();

            if (!ParkourManager.isInParkour(player) || item == null) return;

            if (item.equals(ParkourManager.RESET_ITEM)) {
                ParkourData data = ParkourManager.getPlayerData(player);
                if (data != null) {
                    ParkourCourse course = ParkourManager.getCourseByName(data.getCourseName());
                    if (course != null) {
                        ParkourManager.leaveParkour(player);
                        ParkourManager.startParkour(player, course);
                        sendSingleMessage(player, "§6§lPARKOUR §7» §cVocê recomeçou o parkour!");
                    }
                }
                event.setCancelled(true);
            } else if (item.equals(ParkourManager.LEAVE_ITEM)) {
                ParkourManager.leaveParkour(player);
                event.setCancelled(true);
            } else if (item.equals(ParkourManager.CHECKPOINT_ITEM)) {
                ParkourData data = ParkourManager.getPlayerData(player);
                if (data != null) {
                    int checkpoint = data.getCurrentCheckpoint();
                    if (checkpoint > 0) {
                        ParkourCourse course = ParkourManager.getCourseByName(data.getCourseName());
                        if (course != null) {
                            Location checkpointLoc = course.getCheckpoint(checkpoint).clone();
                            checkpointLoc.setWorld(player.getWorld());
                            player.teleport(checkpointLoc);
                            sendSingleMessage(player, "§6§lPARKOUR §7» §eVocê voltou para seu checkpoint anterior!");
                        }
                    } else {
                        ParkourCourse course = ParkourManager.getCourseByName(data.getCourseName());
                        if (course != null) {
                            Location startLoc = course.getStart().clone();
                            startLoc.setWorld(player.getWorld());
                            player.teleport(startLoc);
                            sendSingleMessage(player, "§6§lPARKOUR §7» §eVocê voltou para o início!");
                        }
                    }
                }
                event.setCancelled(true);
            }
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        Material type = block.getType();

        if (type == Material.GOLD_PLATE) {
            ParkourCourse course = ParkourManager.getCourseByLocation(loc);
            if (course == null) return;

            if (isLocationMatch(loc, course.getStart())) {
                if (!ParkourManager.isInParkour(player)) {
                    ParkourManager.startParkour(player, course);
                }
                return;
            }

            if (isLocationMatch(loc, course.getFinish())) {
                if (ParkourManager.isInParkour(player)) {
                    ParkourData data = ParkourManager.getPlayerData(player);
                    if (data != null && data.getCurrentCheckpoint() >= course.getTotalCheckpoints()) {
                        ParkourManager.finishParkour(player, course);
                    } else {
                        player.sendMessage("§cVocê precisa passar por todos os checkpoints primeiro!");
                        player.sendMessage("§7Checkpoint atual: §e" + ParkourManager.getCurrentCheckpoint(player) + "/" + course.getTotalCheckpoints());
                    }
                }
                return;
            }
        }

        if (type == Material.IRON_PLATE) {
            ParkourCourse course = ParkourManager.getCourseByLocation(loc);
            if (course == null) return;

            for (int i = 1; i <= course.getTotalCheckpoints(); i++) {
                if (isLocationMatch(loc, course.getCheckpoint(i))) {
                    ParkourManager.checkpointReached(player, i, course);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (ParkourManager.isInParkour(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ParkourManager.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!ParkourManager.isInParkour(player)) return;

        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        if (cmd.equals("/fly") || cmd.equals("/voo")) {
            event.setCancelled(true);
            sendSingleMessage(player, "§6§lPARKOUR §7» §cVocê não pode usar fly durante o parkour!");
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!ParkourManager.isInParkour(player)) return;

        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        sendSingleMessage(player, "§6§lPARKOUR §7» §cVocê não pode voar durante o parkour!");
    }

    private boolean isLocationMatch(Location loc1, Location loc2) {
        return Math.abs(loc1.getX() - loc2.getX()) < 1.0 &&
                Math.abs(loc1.getY() - loc2.getY()) < 1.0 &&
                Math.abs(loc1.getZ() - loc2.getZ()) < 1.0;
    }

    private void sendSingleMessage(Player player, String message) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(player);
        String lastMsg = lastMessage.get(player);
        
        if (lastTime != null && message.equals(lastMsg) && (now - lastTime) < 1000) {
            return;
        }
        lastMessageTime.put(player, now);
        lastMessage.put(player, message);
        player.sendMessage(message);
    }
}
