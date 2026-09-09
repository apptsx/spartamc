package com.minecraft.lobby.listener;

import com.minecraft.lobby.Lobby;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSit(PlayerInteractEvent event) {
        if (!event.getAction().toString().equals("RIGHT_CLICK_BLOCK")) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        String name = type.name();

        if (!name.contains("STAIR") && !name.contains("SLAB")) return;

        Player player = event.getPlayer();

        if (player.isSneaking()) return;
        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) return;
        if (player.getVehicle() != null) return;

        event.setCancelled(true);

        Location loc = block.getLocation().add(0.5, 0, 0.5);

        if (name.contains("STAIR")) {
            loc.setY(block.getY() + 0.5);
            org.bukkit.material.Stairs stairs = (org.bukkit.material.Stairs) block.getState().getData();
            switch (stairs.getFacing()) {
                case NORTH: loc.setYaw(180); break;
                case SOUTH: loc.setYaw(0); break;
                case EAST: loc.setYaw(-90); break;
                case WEST: loc.setYaw(90); break;
            }
        } else if (name.contains("SLAB")) {
            loc.setY(block.getY() + 0.1);
        }

        ArmorStand armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        armorStand.setMetadata("sit", new FixedMetadataValue(Lobby.getInstance(), true));

        armorStand.setPassenger(player);

        startBodySync(player, armorStand);
    }

    public static void startBodySync(Player player, ArmorStand armorStand) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getVehicle() == null || !armorStand.equals(player.getVehicle()) || armorStand.isDead()) {
                    cancel();
                    return;
                }
                float headYaw = ((CraftPlayer) player).getHandle().yaw;
                Location aLoc = armorStand.getLocation();
                aLoc.setYaw(headYaw);
                aLoc.setPitch(0);
                armorStand.teleport(aLoc);
            }
        }.runTaskTimer(Lobby.getInstance(), 0L, 1L);
    }

    @EventHandler
    public void onDismount(VehicleExitEvent event) {
        if (event.getVehicle() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getVehicle();
            if (stand.hasMetadata("sit")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        stand.remove();
                    }
                }.runTaskLater(Lobby.getInstance(), 1L);
            }
        }
    }
}
