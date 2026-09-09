package com.minecraft.core.arcade.room.cabin;

import com.minecraft.core.Core;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

@Getter
@Setter
public class Cabin {

    private final String identifier;
    private Location spawn;

    private final Set<Block> blocks = new HashSet<>();

    private DyeColor color;

    public Cabin(String identifier, Location spawn) {
        this.identifier = identifier;
        this.spawn = spawn;
    }

    public boolean isColored() {
        return color != null;
    }

    public boolean build() {
        spawn = spawn.clone().add(0, 5, 0);

        World world = spawn.getWorld();

        try {
            int radius = 2;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius) {
                            // Bordas externas
                            Block block = world.getBlockAt(spawn.clone().add(x, y, z));

                            block.setType(Material.STAINED_GLASS);

                            if (isColored())
                                block.setData(color.getWoolData());

                            blocks.add(block);
                        } else if (Math.abs(x) < radius && Math.abs(y) < radius && Math.abs(z) < radius) {
                            // Esvaziar o interior
                            Block block = world.getBlockAt(spawn.clone().add(x, y, z));

                            block.setType(Material.AIR);
                        }
                    }
                }
            }


            return true;
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Não foi possível gerar a cabine " + identifier, e);

            return false;
        }
    }

    public void delete(JavaPlugin plugin) {
        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Iterator<Block> blockIterator = blocks.iterator();

                    if (!blockIterator.hasNext()) {
                        cancel();
                        return;
                    }

                    while (blockIterator.hasNext()) {
                        Block block = blockIterator.next();

                        block.setType(Material.AIR);

                        blockIterator.remove();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Não foi possível restaurar a cabine " + identifier, e);
        }
    }
}
