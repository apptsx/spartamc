package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.bukkit.api.arcade.ArcadeConfig;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArcadeConfigManager {

    private final Set<ArcadeConfig> configList = new HashSet<>();

    public void create(ArcadeConfig config) {
        remove(config.getCreator());

        configList.add(config);
    }

    public ArcadeConfig of(UUID creator) {
        return configList.stream().filter(config -> config.getCreator().equals(creator)).findFirst().orElse(null);
    }

    public void remove(UUID creator) {
        configList.removeIf(search -> search.getCreator().equals(creator));
    }

    public List<ArcadeConfig> list() {
        return new ArrayList<>(configList);
    }

    public List<ArcadeConfig> list(Predicate<ArcadeConfig> filter) {
        return configList.stream().filter(filter).collect(Collectors.toList());
    }

    public boolean has(UUID creator) {
        return of(creator) != null;
    }
}
