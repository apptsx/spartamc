package com.minecraft.core.bukkit.api.hologram.row;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.touch.TouchHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@Getter
@Setter
@ToString
public class HologramRow {

    private final Hologram hologram;
    private final EntityArmorStand stand;

    private TouchHandler touch;
    private Location location;

    private boolean small = false;

    public HologramRow(Hologram hologram, Location location, String text) {
        this.hologram = hologram;
        this.stand = handleStand(location, text);

        this.location = location;
    }

    protected EntityArmorStand handleStand(Location location, String text) {
        CraftWorld world = (CraftWorld) location.getWorld();

        if (world == null) {
            Core.getLogger().info("Mundo não encontrado!");
            return null;
        }

        EntityArmorStand stand = new EntityArmorStand(world.getHandle());

        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        stand.setCustomName(text);
        stand.setCustomNameVisible(!text.isEmpty());

        stand.setGravity(false);
        stand.setInvisible(true);

        stand.setSmall(small);

        stand.n(false); // Marker

        return stand;
    }

    public void teleport(Location location) {
        this.location = location;

        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public void small(boolean value) {
        this.small = value;

        stand.setSmall(value);
    }

    /* Methods Start */

    public String getText() {
        return stand.getCustomName();
    }

    public void setText(String text) {
        stand.setCustomName(text);
    }

    public int getId() {
        return stand.getId();
    }

    public boolean hasTouch() {
        return touch != null;
    }

    /* Methods End */

    /* Entity Methods */

    public void spawn(Player player) {
        PacketPlayOutSpawnEntityLiving living = new PacketPlayOutSpawnEntityLiving(stand);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(living);

        if (!hologram.isEntityId(getId()))
            hologram.getEntityIds().add(getId());
    }

    public void despawn(Player player) {
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(this.getId());

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);

        if (hologram.isEntityId(getId()))
            hologram.getEntityIds().remove(getId());
    }

    public void respawn(Player player) {
        despawn(player);

        spawn(player);
    }

    public void update(Player player) {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(this.getId(), stand.getDataWatcher(), true);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metadata);
    }
}
