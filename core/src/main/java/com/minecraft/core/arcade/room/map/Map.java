package com.minecraft.core.arcade.room.map;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Map {

    private final int id;
    private final String name;

    private final ArcadeCategory arcade;

    private final transient File source;
    private final transient JsonObject config;

    private final transient int buildLimit;

    private Cuboid area;

    private boolean normal = true;

    private final transient List<SignedLocation> locations = new ArrayList<>();

    @Override
    public boolean equals(Object mapObj) {
        if (this == mapObj) return true;
        if (mapObj == null || getClass() != mapObj.getClass()) return false;

        Map map = (Map) mapObj;

        return map.getName().equals(name) && map.getId() == id;
    }

    public SignedLocation getLocation(String name) {
        return locations.stream().filter(signed -> signed.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Location getLocation(World world, String name) {
        if (!hasLocation(name)) return null;

        return locations.stream()
                .filter(signed -> signed.getName().equalsIgnoreCase(name))
                .map(signed -> signed.getSynthetic().of(world))
                .findFirst()
                .orElse(null);
    }

    public boolean hasLocation(String name) {
        return locations.stream().anyMatch(signed -> signed.getName().equalsIgnoreCase(name));
    }
}
