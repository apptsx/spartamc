package com.minecraft.core.util.list;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WorldUtil {

    public static void setup(World world) {
        world.setPVP(true);

        world.setDifficulty(Difficulty.NORMAL);

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("sendCommandFeedback", "false");
        world.setGameRuleValue("logAdminCommands", "false");

        world.setSpawnFlags(false, false);

        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MIN_VALUE);
        world.setThunderDuration(Integer.MIN_VALUE);

        world.setSpawnLocation(0, 50, 0);
        world.setAutoSave(false);
        world.setTime(6000);

        world.getEntities().forEach(Entity::remove);
    }
}
