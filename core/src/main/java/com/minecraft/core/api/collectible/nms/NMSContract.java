package com.minecraft.core.api.collectible.nms;

import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import lombok.Getter;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class NMSContract {

    @Getter
    private static NMSContract instance;

    public static boolean handle() {
        try {
            instance = new NMS();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract void spawnFirework(Location location, FireworkEffect effect);

    public abstract BalloonEntity createBalloonBat(Player host);

    public abstract BalloonEntity createBalloonArmorStand(Player host, BalloonEntity bat, List<String> frames);

    public abstract void look(Object entity, float yaw, float pitch);

    public abstract void clearPathfinderGoal(Object entity);

    public abstract void registerEntity(Class<?> entity, String name, int id);

    public abstract boolean addEntity(Object... list);

    public abstract void removeEntity(Object... list);

    public abstract Object getHandle(Entity entity);
}
