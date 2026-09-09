package com.minecraft.lobby.util;

import com.minecraft.lobby.Lobby;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ShopSpiralTask {

    private final Location center;
    private final List<Item> items = new ArrayList<>();
    private BukkitRunnable task;
    private int tick = 0;

    public ShopSpiralTask(Location center) {
        this.center = center.clone().add(0, 2.5, 0);
    }

    public void start() {
        Material[] materials = {Material.INK_SACK, Material.GOLD_INGOT, Material.IRON_INGOT, Material.DIAMOND};
        int[] datas = {4, 0, 0, 0};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = datas[i] != 0 ? new ItemStack(materials[i], 1, (short) datas[i]) : new ItemStack(materials[i]);
            Item item = center.getWorld().dropItem(center, stack);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setCustomNameVisible(false);
            item.setVelocity(new Vector(0, 0, 0));
            items.add(item);
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick++;

                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    if (!item.isValid() || item.isDead()) {
                        cancel();
                        return;
                    }

                    double phase = (i * Math.PI / 2) + (tick * 0.05);
                    double radius = 1.5;
                    double height = 2.0 + Math.sin(tick * 0.08) * 1.5;

                    double x = center.getX() + Math.cos(phase) * radius;
                    double z = center.getZ() + Math.sin(phase) * radius;
                    double y = center.getY() + height;

                    item.teleport(new Location(center.getWorld(), x, y, z));
                    item.setVelocity(new Vector(0, 0, 0));
                    item.setPickupDelay(Integer.MAX_VALUE);
                }
            }
        };

        task.runTaskTimer(Lobby.getInstance(), 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Item item : items) {
            if (item.isValid()) {
                item.remove();
            }
        }
        items.clear();
    }
}
