package com.minecraft.arcade.bedwars.structure.generator.objects.type.ore;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.Generator;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@Getter
@Setter
public abstract class OreGenerator extends Generator {

    private final int id;

    private final Arena arena;

    private HologramServer hologram;

    private int updateTime, time;

    public OreGenerator(int id, Arena arena, BedOre ore, int updateTime) {
        super(ore, arena.getLocation("ore_generator_" + ore.name().toLowerCase() + "_" + id)); // ore_generator_diamond_1

        this.id = id;
        this.arena = arena;

        this.updateTime = updateTime;
        this.time = updateTime;

        setLevel(GeneratorLevel.ONE);

        buildHeadRotation();
    }

    public abstract void spawn();

    @Override
    public boolean hasPendentUpdate() {
        return time <= 0;
    }

    public String getIdentifier() {
        return "ore_generator_" + getOre().name().toLowerCase() + "_" + id;
    }

    public void buildHeadRotation() {
        Location location = getLocation();

        /* Caso a localização existir, criar a cabeça */
        if (location != null) {
            location = location.clone().add(0, 2.2, 0);

            ItemStack helmet = new ItemStack(Material.getMaterial(getOre().getMaterial().name() + "_BLOCK"));

            location = arena.getMap().isNormal() ? location.clone().add(0.5, 0, 0.5) : Cuboid.getBlockCenter(location);

            this.hologram = BukkitCore.getManager().getHologram().spawnServer(getIdentifier(), location);

            hologram.setText(Arrays.asList(
                    "§eNível §c" + getLevel().getTag(),
                    getOre().getColor() + "§l" + getOre().getOreName(),
                    "§eGera em §c" + getTime() + "§e segundos"
            ));

            hologram.addAnimatedRow(helmet);

        }
    }
}
