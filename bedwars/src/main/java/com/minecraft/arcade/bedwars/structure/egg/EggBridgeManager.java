package com.minecraft.arcade.bedwars.structure.egg;

import org.bukkit.entity.Egg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EggBridgeManager {

    private final static Map<Egg, EggBridgeTask> tasks = new HashMap<>();

    public static void save(EggBridgeTask task) {
        tasks.put(task.getEgg(), task);
    }

    public static void end(Egg egg) {
        EggBridgeTask task = of(egg);

        if (task != null) {
            task.end();

            tasks.remove(task.getEgg());
        }
    }

    public static EggBridgeTask of(Egg egg) {
        return tasks.get(egg);
    }

    public static List<EggBridgeTask> list() {
        return new ArrayList<>(tasks.values());
    }
}