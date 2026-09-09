package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.bukkit.api.npc.Npc;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NpcManager {

    protected static double cosFOV = Math.cos(Math.toRadians(60));
    protected static double bukkitRange = NumberConversions.square(Bukkit.getViewDistance() << 4);

    private static Field entityCountField;

    static {
        try {
            entityCountField = Entity.class.getDeclaredField("entityCount");
            entityCountField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<Integer, Npc> list = new ConcurrentHashMap<>();

    private boolean inViewOf(Location l, Player player) {
        Vector dir = l.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getEyeLocation().getDirection()) >= cosFOV;
    }

    public boolean canSpawn(Player player, Location loc) {
        return canSpawn(player.getLocation(), loc) && inViewOf(loc, player);
    }

    public boolean canSpawn(Location a, Location b) {
        return a.getWorld().equals(b.getWorld()) && a.distance(b) <= 100;
    }

    public Npc getNPC(int id) {
        return list.get(id);
    }

    public void add(Npc npc) {
        list.put(npc.getEntityId(), npc);
    }

    public void remove(Npc npc) {
        list.remove(npc.getEntityId());
    }

    public Collection<Npc> getNPCs() {
        return list.values();
    }

    public NpcServer spawnServer(Location location) {
        return new NpcServer(location);
    }

    public NpcServer spawnServer(Location location, Property textures) {
        return new NpcServer(location, textures);
    }

    public NpcServer spawnServer(Location location, String value, String signature) {
        return spawnServer(location, new Property("textures", value, signature));
    }

    public NpcClient spawnClient(Player receiver, Location location) {
        return new NpcClient(receiver, location);
    }

    public NpcClient spawnClient(Player receiver, Location location, Property textures) {
        return new NpcClient(receiver, location, textures);
    }

    public NpcClient spawnClient(Player receiver, Location location, String value, String signature) {
        return spawnClient(receiver, location, new Property("textures", value, signature));
    }

    public List<NpcClient> getClients(Player player) {
        return getNPCs().stream().filter(npc -> npc instanceof NpcClient && ((NpcClient) npc).getReceiver().equals(player)).map(npc -> (NpcClient) npc).collect(Collectors.toList());
    }

    public NpcServer getServer(String tag) {
        return getNPCs().stream()
                .filter(npc -> npc instanceof NpcServer && npc.getTag() != null && npc.getTag().equalsIgnoreCase(tag))
                .map(npc -> (NpcServer) npc)
                .findFirst()
                .orElse(null);
    }

    public boolean notExistsServer(String tag) {
        return getServer(tag) == null;
    }

    public void removeFromWorld(World world) {
        for (Npc npc : getNPCs()) {
            if (!npc.isSpawned() || npc.getWorld() == null) continue;

            World npcWorld = npc.getWorld();

            if (!npcWorld.getName().equalsIgnoreCase(world.getName())) continue;

            npc.destroy();
        }
    }

    public void removeClients(Player player) {
        for (Npc npc : getNPCs()) {
            if (!npc.isSpawned()) continue;

            npc.getViewers().remove(player);

            if (npc instanceof NpcClient) {
                NpcClient client = (NpcClient) npc;

                if (client.getReceiver().equals(player))
                    client.destroy();
            }
        }
    }

    public synchronized int nextEntityId() {
        try {
            int currentId = entityCountField.getInt(null);
            entityCountField.set(null, currentId + 1);
            return currentId;
        } catch (Exception e) {
            return -1;
        }
    }
}
