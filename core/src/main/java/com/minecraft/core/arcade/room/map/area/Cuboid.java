package com.minecraft.core.arcade.room.map.area;

import com.minecraft.core.arcade.room.map.location.SyntheticLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Cuboid {

    protected final double lowX, lowY, lowZ;
    protected final double highX, highY, highZ;

    public Cuboid(SyntheticLocation pos1, SyntheticLocation pos2) {
        this.lowX = Math.min(pos1.getX(), pos2.getX());
        this.lowY = Math.min(pos1.getY(), pos2.getY());
        this.lowZ = Math.min(pos1.getZ(), pos2.getZ());
        this.highX = Math.max(pos1.getX(), pos2.getX());
        this.highY = Math.max(pos1.getY(), pos2.getY());
        this.highZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    public Cuboid(Location pos1, Location pos2) {
        this.lowX = Math.min(pos1.getX(), pos2.getX());
        this.lowY = Math.min(pos1.getY(), pos2.getY());
        this.lowZ = Math.min(pos1.getZ(), pos2.getZ());
        this.highX = Math.max(pos1.getX(), pos2.getX());
        this.highY = Math.max(pos1.getY(), pos2.getY());
        this.highZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    public static Location getBlockCenter(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        return new Location(location.getWorld(),
                getRelativeCoordinate(location.getBlockX()),
                getRelativeCoordinate(location.getBlockY()),
                getRelativeCoordinate(location.getBlockZ()),
                location.getYaw(),
                location.getPitch());
    }

    private static double getRelativeCoordinate(int i) {
        return i < 0 ? i - .5 : i + .5;
    }

    public static Location[] getRelativeLocations(Location center) {
        Block origin = center.getBlock();

        Block frontBlock = origin.getRelative(BlockFace.NORTH),
                backBlock = origin.getRelative(BlockFace.SOUTH),
                leftBlock = origin.getRelative(BlockFace.WEST),
                rightBlock = origin.getRelative(BlockFace.EAST);

        return new Location[]{
                frontBlock.getLocation(),  // Frente
                backBlock.getLocation(),   // Atrás
                leftBlock.getLocation(),   // Esquerda
                rightBlock.getLocation()   // Direita
        };
    }

    public static boolean isProtectedArea(Location center, Location check, double frontAndBack, double upRadius, double sideRadius, boolean isAxisX) {
        double minY = center.getBlockY();
        double maxY = center.getBlockY() + upRadius;

        double minFront, maxFront, minSide, maxSide;

        if (isAxisX) {
            minFront = center.getBlockX() - frontAndBack;
            maxFront = center.getBlockX() + frontAndBack;

            minSide = center.getBlockZ() - sideRadius;
            maxSide = center.getBlockZ() + sideRadius;
        } else {
            minFront = center.getBlockZ() - frontAndBack;
            maxFront = center.getBlockZ() + frontAndBack;

            minSide = center.getBlockX() - sideRadius;
            maxSide = center.getBlockX() + sideRadius;
        }

        boolean withinFront, withinSide;

        if (isAxisX) {
            withinFront = check.getBlockX() >= minFront && check.getBlockX() <= maxFront;
            withinSide = check.getBlockZ() >= minSide && check.getBlockZ() <= maxSide;
        } else {
            withinFront = check.getBlockZ() >= minFront && check.getBlockZ() <= maxFront;
            withinSide = check.getBlockX() >= minSide && check.getBlockX() <= maxSide;
        }

        boolean withinY = check.getBlockY() >= minY && check.getBlockY() <= maxY;

        return withinFront && withinSide && withinY;
    }

    public static int getDroppedItemsCount(Location location, Material itemToCompare, double xRadius, double yRadius, double zRadius) {
        Objects.requireNonNull(location, "Location cannot be null");
        World world = location.getWorld();
        if (world == null) return 0;

        return world.getNearbyEntities(location, xRadius, yRadius, zRadius).stream()
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .map(Item::getItemStack)
                .filter(stack -> stack.getType().equals(itemToCompare))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static Set<Block> getBlocksFromCenter(Location location, int sides, int height, int heightBelow) {
        Objects.requireNonNull(location, "Location cannot be null");
        World world = location.getWorld();
        Objects.requireNonNull(world, "World cannot be null");

        int minX = location.getBlockX() - sides;
        int maxX = location.getBlockX() + sides;
        int minY = location.getBlockY() - heightBelow;
        int maxY = location.getBlockY() + height;

        return IntStream.rangeClosed(minX, maxX).boxed()
                .flatMap(x -> IntStream.rangeClosed(minY, maxY).boxed()
                        .flatMap(y -> IntStream.rangeClosed(minX, maxX).boxed()
                                .map(z -> world.getBlockAt(x, y, z))))
                .collect(Collectors.toSet());
    }

    public static Set<Block> getBlocksFromCenter(Entity entity, int radius) {
        return getBlocksFromCenter(entity.getLocation(), radius);
    }

    public static Set<Block> getBlocksFromCenter(Entity entity, int radius, Predicate<Block> filter) {
        return getBlocksFromCenter(entity.getLocation(), radius, filter);
    }

    public static Set<Block> getBlocksFromCenter(Location location, int radius) {
        return getBlocksFromCenter(location, radius, block -> true);
    }

    public static Set<Block> getBlocksFromCenterWithFixedY(Location location, int radius) {
        return getBlocksFromCenterWithFixedY(location, radius, block -> true);
    }

    public static Set<Block> getBlocksFromCenterWithFixedY(Location location, int radius, Predicate<Block> filter) {
        Objects.requireNonNull(location, "Location cannot be null");
        World world = location.getWorld();
        Objects.requireNonNull(world, "World cannot be null");

        int y = location.getBlockY(); // Manter o Y fixo

        return IntStream.rangeClosed(location.getBlockX() - radius, location.getBlockX() + radius).boxed()
                .flatMap(x -> IntStream.rangeClosed(location.getBlockZ() - radius, location.getBlockZ() + radius).boxed()
                        .map(z -> world.getBlockAt(x, y, z))) // O Y permanece constante
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public static Set<Block> getBlocksFromCenter(Location location, int radius, Predicate<Block> filter) {
        Objects.requireNonNull(location, "Location cannot be null");
        World world = location.getWorld();
        Objects.requireNonNull(world, "World cannot be null");

        return IntStream.rangeClosed(location.getBlockX() - radius, location.getBlockX() + radius).boxed()
                .flatMap(x -> IntStream.rangeClosed(location.getBlockY() - radius, location.getBlockY() + radius).boxed()
                        .flatMap(y -> IntStream.rangeClosed(location.getBlockZ() - radius, location.getBlockZ() + radius).boxed()
                                .map(z -> world.getBlockAt(x, y, z))))
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public static boolean hasBlocksInLocation(int radius, Location location, Material... types) {
        Objects.requireNonNull(location, "Location cannot be null");
        Set<Block> blocks = getBlocksFromCenter(location, radius);
        Set<Material> materials = new HashSet<>(Arrays.asList(types));

        return blocks.stream().anyMatch(block -> block != null && materials.contains(block.getType()));
    }

    public static Block getBlockByTypeInLocation(Location location, Material type) {
        Objects.requireNonNull(location, "Location cannot be null");
        Set<Block> blocks = getBlocksFromCenter(location, 15);

        return blocks.stream().filter(block -> block != null && block.getType().equals(type)).findFirst().orElse(null);
    }

    public Set<Block> getBlocks(World world) {
        Objects.requireNonNull(world, "World cannot be null");
        Set<Block> blocks = new HashSet<>();
        for (int x = (int) lowX; x <= highX; x++) {
            for (int y = (int) lowY; y <= highY; y++) {
                for (int z = (int) lowZ; z <= highZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public boolean isInside(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= lowX && x <= highX && y >= lowY && y <= highY && z >= lowZ && z <= highZ;
    }

    public boolean isInside(int x, int y, int z) {
        return x >= this.lowX && x <= this.highX && y >= this.lowY && y <= this.highY && z >= this.lowZ && z <= this.highZ;
    }

    public boolean isOutside(int x, int y, int z) {
        return !isInside(x, y, z);
    }

    public boolean isOutside(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        return isOutside(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Set<Chunk> getChunks(World world) {
        Objects.requireNonNull(world, "World cannot be null");
        Set<Chunk> chunks = new HashSet<>();

        int x1 = (int) lowX & ~0xf;
        int x2 = (int) highX & ~0xf;
        int z1 = (int) lowZ & ~0xf;
        int z2 = (int) highZ & ~0xf;

        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunk.isLoaded()) {
                    chunk.load();
                }
                chunks.add(chunk);
            }
        }

        return chunks;
    }
}