package com.minecraft.core.bukkit.api.hologram.row.animated;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class AnimatedRow extends HologramRow {

    private final ItemStack helmet;

    private int rotate = 0;

    private float headYaw;
    private final float headPitch = 0f;

    private boolean up = true;

    public AnimatedRow(Hologram hologram, Location location, ItemStack helmet) {
        super(hologram, location, "");

        this.helmet = helmet;
    }

    @Override
    public void spawn(Player player) {
        PacketPlayOutSpawnEntityLiving living = new PacketPlayOutSpawnEntityLiving(getStand());
        PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(getStand().getId(), 4, CraftItemStack.asNMSCopy(helmet));

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(living);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(equipmentPacket);
    }

    protected EntityArmorStand handleStand(Location location, String text) {
        CraftWorld world = (CraftWorld) location.getWorld();

        if (world == null) {
            Core.getLogger().info("Mundo não encontrado!");
            return null;
        }

        EntityArmorStand stand = new EntityArmorStand(world.getHandle());

        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        stand.setEquipment(4, CraftItemStack.asNMSCopy(helmet));

        stand.setGravity(false);
        stand.setInvisible(true);

        stand.n(false); // Marker

        return stand;
    }

    public void sendHeadPoseUpdate(Player player, EntityArmorStand armorStand, float headYaw, float headPitch) {
        // Define a posição da cabeça
        armorStand.setHeadPose(new Vector3f(headYaw, headPitch, 0));

        // Obtém o DataWatcher para o EntityArmorStand
        DataWatcher dataWatcher = armorStand.getDataWatcher();

        // Cria um pacote de metadados para atualizar a cabeça
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
                armorStand.getId(),
                dataWatcher,
                true
        );

        // Envia o pacote de metadados para o jogador
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metadataPacket);
    }

    public void rotate() {
        EntityArmorStand nmsArmor = getStand();

        Location loc = nmsArmor.getBukkitEntity().getLocation();

        if (up) {
            if (rotate >= 40) {
                this.up = false;
            }

            loc.add(0.0, 0.01, 0.0);

            if (rotate > 35) {
                loc.setYaw((loc.getYaw() + 3));
            } else if (this.rotate > 30) {
                loc.setYaw((loc.getYaw() + 6));
            } else {
                loc.setYaw((loc.getYaw() + 12));
            }

            nmsArmor.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            updateHeadPoseForViewers(nmsArmor);
            this.rotate++;
        } else {
            if (rotate <= 0) {
                up = true;
            }

            loc.subtract(0.0, 0.01, 0.0);

            if (rotate > 10) {
                loc.setYaw((loc.getYaw() + -12));
            } else if (rotate > 5) {
                loc.setYaw((loc.getYaw() + -6));
            } else {
                loc.setYaw((loc.getYaw() + -3));
            }
            nmsArmor.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            updateHeadPoseForViewers(nmsArmor);
            this.rotate--;
        }
    }

    private float calculateHeadYaw() {
        if (up) {
            if (rotate >= 540) {
                up = false;
            }
            if (rotate > 500) {
                return (float) Math.toRadians(rotate += 1);
            } else if (rotate > 470) {
                return (float) Math.toRadians(rotate += 2);
            } else if (rotate > 450) {
                return (float) Math.toRadians(rotate += 3);
            } else {
                return (float) Math.toRadians(rotate += 4);
            }
        } else {
            if (rotate <= 0) {
                up = true;
            }
            if (rotate > 120) {
                return (float) Math.toRadians(rotate -= 4);
            } else if (rotate > 90) {
                return (float) Math.toRadians(rotate -= 3);
            } else if (rotate > 70) {
                return (float) Math.toRadians(rotate -= 2);
            } else {
                return (float) Math.toRadians(rotate -= 1);
            }
        }
    }

    private void updateVerticalPosition() {
        Location location = getStand().getBukkitEntity().getLocation().clone();

        if (up) {
            location = location.add(0, 0.05, 0);

            getStand().setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else {
            location = location.add(0, -0.05, 0);

            getStand().setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
    }

    private void updateHeadPoseForViewers(EntityArmorStand armorStand) {
        for (Player viewer : getHologram().getViewers()) {
            if (viewer instanceof CraftPlayer) {
                PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;

                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(armorStand);
                connection.sendPacket(teleportPacket);
            }
        }
    }
}
