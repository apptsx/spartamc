package com.minecraft.core.bukkit.api.arcade;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ArcadeConfig {

    private final UUID creator;

    private final ArcadeCategory arcade;
    private final Slot slot;

    private final List<Map> maps = new ArrayList<>();

    private final java.util.Map<String, Object> properties = new HashMap<>();

    public ArcadePayload getPayload() {
        return Core.getArcadeData().read(arcade);
    }

    public boolean isSelected(Map map) {
        return this.maps.stream().anyMatch(search -> search.getId() == map.getId() && search.getName().equalsIgnoreCase(map.getName()));
    }

    public Object getProperty(String key) {
        return properties.get(key.toLowerCase());
    }

    public Object getProperty(String key, Object defaultValue) {
        return properties.getOrDefault(key.toLowerCase(), defaultValue);
    }

    public void setProperty(String key, Object value) {
        properties.put(key.toLowerCase(), value);
    }
}
