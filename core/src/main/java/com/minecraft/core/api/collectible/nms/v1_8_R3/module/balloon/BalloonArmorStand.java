package com.minecraft.core.api.collectible.nms.v1_8_R3.module.balloon;

import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.util.math.NullBoundingBox;
import com.minecraft.core.api.item.Item;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class BalloonArmorStand extends EntityArmorStand implements BalloonEntity {

    private final Player host;
    private final BalloonBat bat;
    private final List<String> frames;
    private float poseY = 0.0F;
    private int count, frame = 1;

    public BalloonArmorStand(Player host, BalloonEntity bat, List<String> frames) {
        super(((CraftWorld) host.getWorld()).getHandle());

        this.host = host;
        this.bat = (BalloonBat) bat;
        this.frames = frames;

        this.setInvisible(true);
        this.setGravity(true);
        this.setBasePlate(true);

        this.setPosition(this.bat.locX, this.bat.locY - 0.2, this.bat.locZ);

        this.setEquipment(4, CraftItemStack.asNMSCopy(Item.of(Material.SKULL_ITEM, 3).skullByBase64(this.frames.get(0))));

        try {
            Field field = net.minecraft.server.v1_8_R3.EntityArmorStand.class.getDeclaredField("bi");
            field.setAccessible(true);
            field.set(this, 2147483647);
        } catch (Exception ignore) {
        }

        this.a(new NullBoundingBox());
    }

    @Override
    public void t_() {
        this.ticksLived = 0;
        super.t_();
        if (this.host == null || !this.host.isOnline()) {
            this.kill();
            return;
        }
        if (this.bat == null || this.bat.dead) {
            this.kill();
            return;
        }

        if (this.frames.size() > 1 && MinecraftServer.currentTick % 10 == 0) {
            if (frame >= this.frames.size()) {
                this.frame = 0;
            }

            this.setEquipment(4, CraftItemStack.asNMSCopy(Item.of(Material.SKULL_ITEM, 3).skullByBase64(this.frames.get(frame++))));
        }

        this.count++;
        if (this.count >= 24) {
            this.count = 0;
        }
        this.poseY += 2.5F;

        super.setHeadPose(new Vector3f(0.0F, poseY, 0.0F));

        this.setPosition(this.bat.locX, this.bat.locY - 0.2, this.bat.locZ);
    }

    @Override
    public void die() {
    }

    @Override
    public void kill() {
        this.dead = true;
    }

    @Override
    public boolean isInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public void setCustomName(String customName) {
    }


    @Override
    public void setCustomNameVisible(boolean visible) {
    }

    @Override
    public boolean a(EntityHuman human, Vec3D vec3d) {
        return false;
    }

}