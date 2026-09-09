package com.minecraft.core.api.collectible.nms.module.balloon;

import org.bukkit.entity.Entity;

public interface BalloonEntity {

    Entity getBukkitEntity();

    void kill();
}
