package com.minecraft.core.arcade.room.map.location;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@Setter
@RequiredArgsConstructor
public class SyntheticLocation {

    private final double x, y, z;
    private final float yaw, pitch;

    private boolean axisX = false;

    public Location of(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
