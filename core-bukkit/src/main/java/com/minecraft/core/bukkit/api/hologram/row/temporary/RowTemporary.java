package com.minecraft.core.bukkit.api.hologram.row.temporary;

import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import lombok.Getter;
import org.bukkit.Location;


@Getter
public class RowTemporary extends HologramRow {

    private final String preview;

    public RowTemporary(Hologram hologram, Location location, String preview) {
        super(hologram, location, preview);

        this.preview = preview;
    }
}
