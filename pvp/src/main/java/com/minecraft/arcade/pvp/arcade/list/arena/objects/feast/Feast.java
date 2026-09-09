package com.minecraft.arcade.pvp.arcade.list.arena.objects.feast;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Feast {

    private final Room arena;
    private final Location location;

    private List<Block> placedBlocks = new ArrayList<>();

    private int time = 60 * 5;
    private boolean spawned = false;

    private static final int MAX_ITEMS_IN_CHEST = 7;

    public static ItemStack[] getFeastStacks() {
        return new ItemStack[]{
                Item.of(Material.LEATHER_CHESTPLATE),
                Item.of(Material.EXP_BOTTLE).amount(12),
                Item.of(Material.STONE_SWORD),
                Item.of(Material.GOLDEN_APPLE).amount(5),
                Item.of(Material.BOW),
                Item.of(Material.EXP_BOTTLE).amount(5),
                Item.of(Material.ARROW).amount(12),
                Item.of(Material.SNOW_BALL).amount(16),
                Item.of(Material.POTION, 16385),
                Item.of(Material.POTION, 8225)
        };
    }

    public void spawn() {
        makeChestsShapes(location, 4);
        location.getWorld().strikeLightningEffect(location);

        this.spawned = true;
        this.time = 60;

        arena.send("§aO feast foi spawnado.");
    }

    public void destruct() {
        Core.getPlatform().runSync(() -> {
            placedBlocks.forEach(block -> {
                /* Limpando baú */
                if (block.getType().equals(Material.CHEST)) {
                    Chest chest = (Chest) block.getState();

                    chest.getBlockInventory().clear();
                }

                block.setType(Material.AIR);
            });

            placedBlocks.clear();
        });

        this.spawned = false;
        this.time = 60 * 5;

        arena.send("§cO feast foi removido.");
    }

    protected void fillChests(Inventory menu) {
        int itemsArraySize = getFeastStacks().length;

        for (int i = 0; i < MAX_ITEMS_IN_CHEST; i++) {
            int randomItemPosition = Core.RANDOM.nextInt(itemsArraySize);
            int randomPosition = Core.RANDOM.nextInt(menu.getSize());

            ItemStack randomItem = getFeastStacks()[randomItemPosition];

            if (menu.contains(randomItem)) continue;

            menu.setItem(randomPosition, randomItem);
        }
    }

    protected void makeChestsShapes(Location location, int size) {
        int halfSize = size / 2;

        /* Surgir baús */
        Core.getPlatform().runSync(() -> {
            for (Block block : Cuboid.getBlocksFromCenterWithFixedY(location, halfSize)) {
                Location blockLocation = block.getLocation();

                if (blockLocation.getBlockX() == location.getBlockX() && blockLocation.getBlockY() == location.getBlockY() && blockLocation.getBlockZ() == location.getBlockZ())
                    continue;

                if ((blockLocation.getBlockX() + blockLocation.getBlockZ()) % 2 == 0) {
                    block.setType(Material.CHEST);

                    Chest chest = (Chest) block.getState();

                    fillChests(chest.getBlockInventory());

                    block.setMetadata("feast", new FixedMetadataValue(PvP.getInstance(), true));

                    placedBlocks.add(block);
                }
            }
        });

        /* Surgir mesa de encantamento */
        Core.getPlatform().runSync(() -> {
            Block block = location.getBlock();

            block.setType(Material.ENCHANTMENT_TABLE);
            block.setMetadata("feast", new FixedMetadataValue(PvP.getInstance(), true));

            placedBlocks.add(block);
        });
    }
}
