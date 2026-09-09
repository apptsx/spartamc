package com.minecraft.arcade.bedwars.structure.generator;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.util.list.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

@Getter
@Setter
public abstract class Generator {

    private final BedOre ore;
    private final Location location;

    private GeneratorLevel level;

    private int maxStackSize;

    public Generator(BedOre ore, Location location) {
        this.ore = ore;
        this.location = location;

        this.level = GeneratorLevel.NONE;

        this.maxStackSize = -1;
    }

    public abstract boolean hasPendentUpdate();

    public ItemStack getDrop() {
        return new ItemStack(getOre().getMaterial());
    }

    public void drop(Arena arena, Location... centers) {
        int currentDroppedItems = Cuboid.getDroppedItemsCount(getLocation(), getOre().getMaterial(), 2, 1, 2);

        if (currentDroppedItems >= getMaxStackSize()) return;

        for (Location center : centers) {
            World world = center.getWorld();

            if (world == null) return;

            // Centraliza a localização exatamente no meio do bloco
            center = arena.getMap().isNormal() ? center.clone().add(0.5, 0, 0.5) : Cuboid.getBlockCenter(center);

            center = center.clone().add(0, 1.0, 0);

            ItemStack stack = getDrop();

            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(getOre().name().toLowerCase() + "-" + StringUtil.generateExclusiveCode(6));
            stack.setItemMeta(meta);

            Item item = world.dropItem(center, stack);

            item.teleport(center);
            item.setVelocity(new Vector(0, 0, 0));
        }
    }
}
