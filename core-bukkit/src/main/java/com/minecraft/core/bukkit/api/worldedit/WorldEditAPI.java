package com.minecraft.core.bukkit.api.worldedit;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.*;

public class WorldEditAPI {

    public static List<Location> makePyramid(Location position, int size, boolean filled) {
        Set<Location> locations = new HashSet<>();
        int height = size;

        for (int y = 0; y <= height; ++y) {
            size--;
            for (int x = 0; x <= size; ++x) {
                for (int z = 0; z <= size; ++z) {
                    if (filled || z == size || x == size) {
                        locations.add(position.clone().add(x, y, z));
                        locations.add(position.clone().add(-x, y, z));
                        locations.add(position.clone().add(x, y, -z));
                        locations.add(position.clone().add(-x, y, -z));
                    }
                }
            }
        }
        return new ArrayList<>(locations);
    }

    public static List<Location> makeSphere(Location pos, double radiusX, double radiusY, double radiusZ, boolean filled) {
        Set<Location> locations = new HashSet<>();

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        for (int x = 0; x <= ceilRadiusX; ++x) {
            double xn = x * invRadiusX;
            for (int y = 0; y <= ceilRadiusY; ++y) {
                double yn = y * invRadiusY;
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    double zn = z * invRadiusZ;
                    double distanceSq = lengthSq(xn, yn, zn);

                    if (distanceSq <= 1 && (filled || isSurface(x, y, z, ceilRadiusX, ceilRadiusY, ceilRadiusZ, invRadiusX, invRadiusY, invRadiusZ))) {
                        addSymmetricalLocations(locations, pos, x, y, z);
                    }
                }
            }
        }
        return new ArrayList<>(locations);
    }

    private static boolean isSurface(int x, int y, int z, int ceilX, int ceilY, int ceilZ, double invX, double invY, double invZ) {
        return lengthSq((x + 1) * invX, y * invY, z * invZ) > 1 || lengthSq(x * invX, (y + 1) * invY, z * invZ) > 1 || lengthSq(x * invX, y * invY, (z + 1) * invZ) > 1;
    }

    private static void addSymmetricalLocations(Set<Location> locations, Location pos, int x, int y, int z) {
        locations.add(pos.clone().add(x, y, z));
        locations.add(pos.clone().add(-x, y, z));
        locations.add(pos.clone().add(x, -y, z));
        locations.add(pos.clone().add(x, y, -z));
        locations.add(pos.clone().add(-x, -y, z));
        locations.add(pos.clone().add(x, -y, -z));
        locations.add(pos.clone().add(-x, y, -z));
        locations.add(pos.clone().add(-x, -y, -z));
    }

    public static List<Location> makeCylinder(Location pos, double radiusX, double radiusZ, int height, boolean filled) {
        Set<Location> locations = new HashSet<>();

        radiusX += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        for (int x = 0; x <= ceilRadiusX; ++x) {
            double xn = x * invRadiusX;
            for (int z = 0; z <= ceilRadiusZ; ++z) {
                double zn = z * invRadiusZ;
                double distanceSq = lengthSq(xn, zn);

                if (distanceSq <= 1 && (filled || lengthSq((x + 1) * invRadiusX, zn) > 1 || lengthSq(xn, (z + 1) * invRadiusZ) > 1)) {
                    for (int y = 0; y < height; ++y) {
                        addSymmetricalLocations(locations, pos.clone().add(0, y, 0), x, 0, z);
                    }
                }
            }
        }
        return new ArrayList<>(locations);
    }

    public static List<Location> getBlocksBetween(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return Collections.emptyList();

        List<Location> locations = new ArrayList<>();
        int lowerX = Math.min(loc1.getBlockX(), loc2.getBlockX()), lowerY = Math.min(loc1.getBlockY(), loc2.getBlockY()), lowerZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int higherX = Math.max(loc1.getBlockX(), loc2.getBlockX()), higherY = Math.max(loc1.getBlockY(), loc2.getBlockY()), higherZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = lowerX; x <= higherX; x++) {
            for (int y = lowerY; y <= higherY; y++) {
                for (int z = lowerZ; z <= higherZ; z++) {
                    locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }
        return locations;
    }

    public static List<Location> getWalls(Location loc1, Location loc2) {
        List<Location> locations = new ArrayList<>();
        int lowerX = Math.min(loc1.getBlockX(), loc2.getBlockX()), lowerY = Math.min(loc1.getBlockY(), loc2.getBlockY()), lowerZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int higherX = Math.max(loc1.getBlockX(), loc2.getBlockX()), higherY = Math.max(loc1.getBlockY(), loc2.getBlockY()), higherZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = lowerX; x <= higherX; x++) {
            for (int y = lowerY; y <= higherY; y++) {
                for (int z = lowerZ; z <= higherZ; z++) {
                    if (x == lowerX || x == higherX || z == lowerZ || z == higherZ) {
                        locations.add(new Location(loc1.getWorld(), x, y, z));
                    }
                }
            }
        }
        return locations;
    }

    private static double lengthSq(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    private static double lengthSq(double x, double z) {
        return x * x + z * z;
    }

    public static void setBlock(Location location, Material material, byte data) {
        setBlock(location, material, data, true);
    }

    public static void setBlock(Location location, Material material, byte data, boolean forceLight) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        if (y >= location.getWorld().getMaxHeight() || y < 0) return;

        BlockPosition blockPosition = new BlockPosition(x, y, z);
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(material.getId() + (data << 12));
        ChunkSection chunkSection = nmsChunk.getSections()[y >> 4];

        if (chunkSection == null) {
            chunkSection = new ChunkSection(y >> 4 << 4, true);
            nmsChunk.getSections()[y >> 4] = chunkSection;
        }
        chunkSection.setType(x & 15, y & 15, z & 15, blockData);

        if (forceLight) setLightLevel(location, 15);

        nmsWorld.notify(blockPosition);
    }

    public static void setLightLevel(Location location, int level) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        if (y >= location.getWorld().getMaxHeight() || y < 0) return;

        BlockPosition blockPosition = new BlockPosition(x, y, z);
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        ChunkSection chunkSection = nmsChunk.getSections()[y >> 4];

        if (chunkSection == null) {
            chunkSection = new ChunkSection(y >> 4 << 4, true);
            nmsChunk.getSections()[y >> 4] = chunkSection;
        }
        nmsWorld.a(EnumSkyBlock.BLOCK, blockPosition, level);
    }

    public static int findHighestBlockY(World world, int x, int z) {
        return world.getHighestBlockAt(x, z).getY();
    }
}
