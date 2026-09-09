package com.minecraft.core.api.collectible.util.math;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class RelativeLocation implements Cloneable {

    private boolean backward;
    private final double forwardBackwards;
    private final double leftRight;
    private final double upDown;
    private float yaw;

    public RelativeLocation(double upDown, double leftRight, double forwardBackwards) {
        this(upDown, leftRight, forwardBackwards, 0.0F);
    }

    public RelativeLocation(double upDown, double leftRight, double forwardBackwards, float yaw) {
        this.upDown = upDown;
        this.leftRight = leftRight;
        this.forwardBackwards = forwardBackwards;
        this.yaw = yaw;
    }

    public double getForwardBackward() {
        return this.forwardBackwards;
    }

    public RelativeLocation setBackward(boolean backward) {
        this.backward = backward;
        return this;
    }

    public RelativeLocation setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public Location getFromRelative(Location location) {
        Location relative = this.getLocation(location, this.leftRight, this.upDown, this.forwardBackwards);
        if (this.yaw != 0.0f) {
            relative.setYaw(relative.getYaw() + this.yaw);
        }
        if (this.backward) {
            relative.setYaw(-relative.getYaw());
        }

        return relative;
    }

    private Location getLocation(Location location, double x, double y, double z) {
        Location clone = location.clone();
        float yaw = clone.getYaw();
        if (clone.getYaw() < 0.0f) {
            yaw = Math.abs(yaw) + 180.0f;
        }

        double cos = CosSineTable.cos((int) yaw);
        double sine = CosSineTable.sin((int) yaw);
        return clone.add(0.0D, y, 0.0D).add(cos * x, 0.0D, sine * x).add((-sine) * z, 0.0D, cos * z);
    }

    @Override
    public RelativeLocation clone() {
        return new RelativeLocation(this.upDown, this.leftRight, this.forwardBackwards).setYaw(getYaw()).setBackward(isBackward());
    }
}