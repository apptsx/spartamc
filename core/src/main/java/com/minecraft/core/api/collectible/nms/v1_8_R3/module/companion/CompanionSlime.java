package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion;

import com.minecraft.core.api.collectible.nms.module.companion.CompanionEntity;
import com.minecraft.core.api.collectible.nms.path.PathfinderGoalFollowCompanion;
import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.type.companion.type.CompanionType;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class CompanionSlime extends EntityCreature implements IAnimal, CompanionEntity {

    protected boolean inIdle;
    protected CompanionOperator companion;
    protected List<CompanionModel> models;
    protected int maxFrames, maxIdleFrames;
    private final double moveSpeed;
    private boolean moveAnimation, idleAnimation;
    private int currentFrame;
    private int currentIdleFrame;

    public CompanionSlime(CompanionOperator companion) {
        super(((CraftWorld) companion.getHost().getWorld()).getHandle());

        this.companion = companion;
        this.models = new ArrayList<>();

        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(20.0D);
        this.moveSpeed = .60;

        Location petLocation = companion.getHost().getLocation().clone().add(2, 0, 0.88);
        if (!petLocation.getBlock().isEmpty()) {
            petLocation.subtract(2.0, 0, 0.88);
        }

        getBukkitEntity().teleport(petLocation);
        NMS.getInstance().clearPathfinderGoal(this);

        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalFollowCompanion(companion, this, moveSpeed));
    }

    protected void spawn() {
        List<Object> entities = new ArrayList<>(Collections.singleton(this));
        entities.addAll(this.models.stream().map(structure -> NMS.getInstance().getHandle(structure.getStand())).collect(Collectors.toList()));

        for (Object entity : entities) {
            NMS.getInstance().addEntity(entity);
        }

        CompanionCollectible companionCollectible = (CompanionCollectible) this.companion.getCollectible();
        CompanionType companionType = CompanionType.fromType(companionCollectible.getClass());
        String companionName = companionType != null ? companionType.getDefaultName() : companionCollectible.getName();
        
        this.setCompanionName(companionName);
        this.moveToSlime();
    }

    @Override
    protected void h() {
        super.h();
        this.datawatcher.watch(0, (byte) 0x20);
        this.datawatcher.a(16, (byte) 0);
    }

    @Override
    public void t_() {
        super.t_();
        this.repeatTask();
    }

    @Override
    public void g(float f, float f1) {
        this.S = 0.5F;
        super.g(f, f1);
    }

    @Override
    public void die() {
    }

    @Override
    public void kill() {
        this.dead = true;
    }

    @Override
    protected boolean a(EntityHuman entityhuman) {
        return false;
    }

    public void repeatTask() {
        if (this.companion == null || this.companion.getHost() == null || !this.companion.getHost().isOnline()) {
            this.kill();
            return;
        }

        this.inIdle = this.lastX == this.locX && this.lastY == this.locY && this.lastZ == this.locZ;
        double currentSpeed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        if (currentSpeed != this.moveSpeed) {
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.moveSpeed);
        }

        CompanionCollectible collectible = (CompanionCollectible) this.companion.getCollectible();

        if (this.inIdle && !collectible.hasIdleAnimation()) {
            if (this.maxFrames == 0) {
                return;
            }

            collectible.getKeyFrames().get(0).forEach(this::animate);
            return;
        }

        this.toggleType(this.inIdle);
        this.getFrames(this.inIdle).values().stream().filter(value -> !value.isEmpty()).forEach(value -> this.animate(value.get(this.getCurrent(this.inIdle))));
        this.increase(this.inIdle);

        moveToSlime();
    }

    private Map<Integer, List<CompanionAnimation>> getFrames(boolean inIdle) {
        CompanionCollectible collectible = (CompanionCollectible) this.companion.getCollectible();

        return !inIdle ? collectible.getFrames() : collectible.getIdleFrames();
    }

    private int getCurrent(boolean inIdle) {
        return !inIdle ? this.currentFrame : this.currentIdleFrame;
    }

    private void toggleType(boolean inIdle) {
        if (!inIdle) {
            if (this.idleAnimation) {
                this.currentFrame = 0;
                this.idleAnimation = false;
                this.moveAnimation = true;
            }

            if (this.currentFrame >= this.maxFrames) {
                this.currentFrame = 0;
            }
        } else {
            if (this.moveAnimation) {
                this.currentIdleFrame = 0;
                this.moveAnimation = false;
                this.idleAnimation = true;
            }

            if (this.currentIdleFrame >= this.maxIdleFrames) {
                this.currentIdleFrame = 0;
            }
        }
    }

    private void animate(CompanionAnimation companionAnimation) {
        CompanionModel model = this.getStructureByName(companionAnimation.getName());

        if (model == null)
            System.err.println("Failed to find model: " + companionAnimation.getName());
        else {
            switch (companionAnimation.getType()) {
                case ARM_RIGHT:
                    model.getStand().setRightArmPose(companionAnimation.getAngle());
                    break;
                case ARM_LEFT:
                    model.getStand().setLeftArmPose(companionAnimation.getAngle());
                    break;
                case LEG_RIGHT:
                    model.getStand().setRightLegPose(companionAnimation.getAngle());
                    break;
                case LEG_LEFT:
                    model.getStand().setLeftLegPose(companionAnimation.getAngle());
                    break;
                case HEAD:
                    model.getStand().setHeadPose(companionAnimation.getAngle());
            }
        }
    }

    private void increase(boolean inIdle) {
        if (!inIdle) {
            this.currentFrame++;
        } else {
            this.currentIdleFrame++;
        }
    }

    public void moveToSlime() {
        this.models.forEach(structure -> structure.getStand().teleport(structure.getLocation().getFromRelative(this.getBukkitEntity().getLocation())));
    }

    public void updateRelativeLocation(Location playerLocation) {
        this.models.forEach(structure -> structure.getStand().teleport(structure.getLocation().getFromRelative(playerLocation)));
    }

    protected CompanionModel buildPart(String name, RelativeLocation relativeLocation, boolean small) {
        CompanionStand stand = new CompanionStand(this, small);
        Location location = relativeLocation.getFromRelative(getBukkitEntity().getLocation());
        stand.setPosition(location.getX(), location.getY(), location.getZ());
        NMS.getInstance().look(stand, location.getYaw(), location.getPitch());
        CompanionModel structure = new CompanionModel(name, (ArmorStand) stand.getBukkitEntity(), relativeLocation);
        this.models.add(structure);
        return structure;
    }

    private CompanionModel getStructureByName(String name) {
        return this.models.stream().filter(structure -> structure.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public boolean isInvulnerable(DamageSource damagesource) {
        return true;
    }

    @Override
    public void setCustomName(String s) {
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
    }

    @Override
    public boolean d(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void setInvisible(boolean flag) {
    }

    public void a(NBTTagCompound nbttagcompound) {
    }

    public void b(NBTTagCompound nbttagcompound) {
    }

    public boolean c(NBTTagCompound nbttagcompound) {
        return false;
    }

    public boolean d(NBTTagCompound nbttagcompound) {
        return false;
    }

    public void e(NBTTagCompound nbttagcompound) {
    }

    public void f(NBTTagCompound nbttagcompound) {
    }


    protected static class CompanionStand extends EntityArmorStand implements CompanionEntity {

        private final CompanionSlime slime;

        public CompanionStand(CompanionSlime entity, boolean small) {
            super(entity.world);
            this.slime = entity;

            this.setArms(true);
            this.setInvisible(true);
            this.setSmall(small);
            this.setGravity(true);
            this.setBasePlate(false);
        }

        @Override
        public void t_() {
            this.ticksLived = 0;
            super.t_();
            if (this.slime == null || this.slime.dead) {
                this.dead = true;
            }
        }

        @Override
        public void setCompanionName(String name) {
            super.setCustomName(name);
            super.setCustomNameVisible(!name.isEmpty());
        }

        @Override
        public void kill() {
            this.slime.kill();
        }

        @Override
        public void die() {
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
}
