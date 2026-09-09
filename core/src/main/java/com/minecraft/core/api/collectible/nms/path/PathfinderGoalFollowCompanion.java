package com.minecraft.core.api.collectible.nms.path;

import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class PathfinderGoalFollowCompanion extends PathfinderGoal {

    private final CompanionOperator companion;
    private final Player owner;
    private final EntityInsentient entity;
    private final double speed;
    private final double distance = 2.0;
    private final int stay = 1;

    public PathfinderGoalFollowCompanion(CompanionOperator companion, EntityInsentient entity, double speed) {
        this.companion = companion;
        this.owner = companion.getHost();

        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean a() {
        if (!companion.update()) {
            return false;
        }

        if (!this.entity.getBukkitEntity().getWorld().equals(this.owner.getLocation().getWorld())) {
            return false;
        }

        this.c();
        return true;
    }

    @Override
    public void c() {
        Location location = this.owner.getLocation();
        int distance = (int) location.distance(this.entity.getBukkitEntity().getLocation());
        if (distance >= 15.0 && this.owner.isOnGround()) {
            this.entity.setPosition(location.getX(), location.getY(), location.getZ());
            this.entity.yaw = location.getYaw();
            this.entity.pitch = location.getPitch();
            return;
        }

        PathEntity pathEntity = this.entity.getNavigation().a(location.getX() + stay, location.getY(), location.getZ() + stay);
        if (pathEntity != null) {
            if (distance > this.distance) {
                this.entity.getNavigation().a(pathEntity, speed);
                this.entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            } else {
                this.entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.0);
            }
        }
    }
}
