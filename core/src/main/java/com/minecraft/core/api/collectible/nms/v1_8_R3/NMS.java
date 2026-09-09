package com.minecraft.core.api.collectible.nms.v1_8_R3;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.nms.NMSContract;
import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.nms.module.companion.CompanionEntities;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.balloon.BalloonArmorStand;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.balloon.BalloonBat;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list.*;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.firework.CustomEntityFirework;
import com.minecraft.core.api.collectible.type.companion.list.*;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.ReflectionUtil;
import com.minecraft.core.util.list.reflection.FieldAccessor;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;

public class NMS extends NMSContract {

    private final FieldAccessor<Map> CLASS_TO_ID;
    private final FieldAccessor<Map> CLASS_TO_NAME;
    private final FieldAccessor<List> PATHFINDERGOAL_B, PATHFINDERGOAL_C;

    public NMS() {
        CLASS_TO_ID = ReflectionUtil.getFieldAccessor(EntityTypes.class, "f", Map.class);
        CLASS_TO_NAME = ReflectionUtil.getFieldAccessor(EntityTypes.class, "d", Map.class);
        PATHFINDERGOAL_B = ReflectionUtil.getFieldAccessor(PathfinderGoalSelector.class, 0, List.class);
        PATHFINDERGOAL_C = ReflectionUtil.getFieldAccessor(PathfinderGoalSelector.class, 1, List.class);

        // Balloon Register
        registerEntity(BalloonBat.class, "Bat", 65);

        // Companion Register
        registerEntity(CompanionSlime.class, "Slime", 55);

        CompanionEntities.setTypes(
                new CompanionEntities.CompanionEntityModel(ChimpanzeeCompanion.class, ChimpanzeeEntity.class),
                new CompanionEntities.CompanionEntityModel(MythicDragonCompanion.class, MythicDragonEntity.class),
                new CompanionEntities.CompanionEntityModel(PugCompanion.class, PugEntity.class),
                new CompanionEntities.CompanionEntityModel(BoxCompanion.class, BoxEntity.class),
                new CompanionEntities.CompanionEntityModel(FoxCompanion.class, FoxEntity.class),
                new CompanionEntities.CompanionEntityModel(LionCompanion.class, LionEntity.class),
                new CompanionEntities.CompanionEntityModel(PerryThePlatypusCompanion.class, PerryThePlatypusEntity.class),
                new CompanionEntities.CompanionEntityModel(GiraffeCompanion.class, GiraffeEntity.class),
                new CompanionEntities.CompanionEntityModel(R2D2Companion.class, R2D2Entity.class),
                new CompanionEntities.CompanionEntityModel(BB8Companion.class, BB8Entity.class),
                new CompanionEntities.CompanionEntityModel(FireDragonCompanion.class, FireDragonEntity.class),
                new CompanionEntities.CompanionEntityModel(PenguinCompanion.class, PenguinEntity.class),
                new CompanionEntities.CompanionEntityModel(PandaCompanion.class, PandaEntity.class),
                new CompanionEntities.CompanionEntityModel(PolarBearCompanion.class, PolarBearEntity.class),
                new CompanionEntities.CompanionEntityModel(MiniMeCompanion.class, MiniMeEntity.class),
                new CompanionEntities.CompanionEntityModel(DuckCompanion.class, DuckEntity.class),
                new CompanionEntities.CompanionEntityModel(DiglettCompanion.class, DiglettEntity.class),
                new CompanionEntities.CompanionEntityModel(IceDragonCompanion.class, IceDragonEntity.class),
                new CompanionEntities.CompanionEntityModel(MagicDragonCompanion.class, MagicDragonEntity.class),
                new CompanionEntities.CompanionEntityModel(GorillaCompanion.class, GorillaEntity.class),
                new CompanionEntities.CompanionEntityModel(TurtleCompanion.class, TurtleEntity.class),
                new CompanionEntities.CompanionEntityModel(KoalaCompanion.class, KoalaEntity.class),
                new CompanionEntities.CompanionEntityModel(DragonCompanion.class, DragonEntity.class),
                new CompanionEntities.CompanionEntityModel(BeeCompanion.class, BeeEntity.class)
        );
    }

    @Override
    public void spawnFirework(Location location, FireworkEffect effect) {
        CustomEntityFirework entityFirework = new CustomEntityFirework(((CraftWorld) location.getWorld()).getHandle());

        Firework firework = (Firework) entityFirework.getBukkitEntity();
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(effect);
        firework.setFireworkMeta(meta);

        entityFirework.setPosition(location.getX(), location.getY(), location.getZ());
        entityFirework.setInvisible(true);
        entityFirework.world.addEntity(entityFirework, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public BalloonEntity createBalloonBat(Player host) {
        BalloonBat bat = new BalloonBat(host);

        if (addEntity(bat))
            return bat;

        return null;
    }

    @Override
    public BalloonEntity createBalloonArmorStand(Player host, BalloonEntity bat, List<String> frames) {
        BalloonArmorStand stand = new BalloonArmorStand(host, bat, frames);

        return addEntity(stand) ? stand : null;
    }

    @Override
    public void look(Object entity, float yaw, float pitch) {
        if (entity instanceof Entity)
            entity = ((CraftEntity) entity).getHandle();

        yaw = Util.clampYaw(yaw);
        net.minecraft.server.v1_8_R3.Entity handle = (net.minecraft.server.v1_8_R3.Entity) entity;
        handle.yaw = yaw;
        handle.pitch = pitch;
        if (handle instanceof EntityLiving) {
            ((EntityLiving) handle).aJ = yaw;
            if (handle instanceof EntityHuman) {
                ((EntityHuman) handle).aI = yaw;
            }
            ((EntityLiving) handle).aK = yaw;
        }

    }

    @Override
    public void clearPathfinderGoal(Object entity) {
        if (entity instanceof Entity) {
            entity = ((CraftEntity) entity).getHandle();
        }

        net.minecraft.server.v1_8_R3.Entity handle = (net.minecraft.server.v1_8_R3.Entity) entity;
        if (handle instanceof EntityInsentient) {
            EntityInsentient entityInsentient = (EntityInsentient) handle;
            PATHFINDERGOAL_B.get(entityInsentient.goalSelector).clear();
            PATHFINDERGOAL_C.get(entityInsentient.targetSelector).clear();
        }
    }

    @Override
    public void registerEntity(Class<?> entity, String name, int id) {
        CLASS_TO_ID.get(null).put(entity, id);
        CLASS_TO_NAME.get(null).put(entity, "CLB-" + name);
    }

    @Override
    public boolean addEntity(Object... list) {
        boolean bol = true;
        try {
            for (Object entity : list) {
                net.minecraft.server.v1_8_R3.Entity handle = (net.minecraft.server.v1_8_R3.Entity) entity;

                handle.getBukkitEntity().setMetadata("CLB_ENTITY", new FixedMetadataValue(Core.getJavaPlugin(), true));
                bol = handle.world.addEntity(handle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }

            return bol;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void removeEntity(Object... list) {
        try {
            for (Object entity : list) {
                net.minecraft.server.v1_8_R3.Entity handle = (net.minecraft.server.v1_8_R3.Entity) entity;

                handle.world.removeEntity(handle);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public Object getHandle(Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }
}