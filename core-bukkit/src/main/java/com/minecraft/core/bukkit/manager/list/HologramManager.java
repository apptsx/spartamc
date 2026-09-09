package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.util.list.StringUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class HologramManager {

    protected final double range = NumberConversions.square(Bukkit.getViewDistance() << 4);

    private final Map<Integer, Hologram> holograms = new ConcurrentHashMap<>();

    public boolean canSpawn(Hologram hologram, Location location) {
        Location hologramLocation = hologram.getLocation();

        return location.getWorld().equals(hologramLocation.getWorld())
                && location.distanceSquared(hologramLocation) <= range;
    }

    public HologramServer getServer(String tag) {
        return (HologramServer) getHolograms().values().stream()
                .filter(hologram -> hologram.getClass().isAssignableFrom(HologramServer.class) && hologram.getTag().equalsIgnoreCase(tag))
                .findFirst()
                .orElse(null);
    }

    public List<HologramClient> getClients() {
        List<Hologram> holograms = getHolograms().values().stream()
                .filter(hologram -> hologram instanceof HologramClient)
                .collect(Collectors.toList());

        List<HologramClient> clients = new ArrayList<>();

        holograms.forEach(hologram -> clients.add((HologramClient) hologram));

        return clients;
    }

    public List<HologramClient> getClients(Player player) {
        return getClients().stream().filter(client -> client.getReceiver().getUniqueId().equals(player.getUniqueId())).collect(Collectors.toList());
    }

    public List<HologramServer> getServers(String tag) {
        List<Hologram> holograms = getHolograms().values().stream()
                .filter(hologram -> hologram instanceof HologramServer && hologram.getTag().equalsIgnoreCase(tag))
                .collect(Collectors.toList());

        List<HologramServer> servers = new ArrayList<>();

        holograms.forEach(hologram -> servers.add((HologramServer) hologram));

        return servers;
    }

    public HologramClient getClient(Player player, String tag) {
        return getClients(player).stream().filter(client -> client.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);
    }

    public boolean notExistsClient(Player player, String tag) {
        HologramClient client = getClient(player, tag);

        return client == null || !client.getReceiver().equals(player);
    }

    public boolean notExistsServer(String tag) {
        HologramServer server = getServer(tag);

        return server == null || server.getLocation() == null;
    }

    public void removeFromWorld(World world) {
        for (Hologram hologram : holograms.values()) {
            if (hologram == null || hologram.getWorld() == null) continue;

            World hologramWorld = hologram.getWorld();

            if (!hologramWorld.equals(world)) continue;

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getWorld().equals(hologramWorld))
                    .forEach(hologram::despawnTo);
        }
    }

    public void removeServer(String tag) {
        HologramServer server = getServer(tag);
        if (server == null) return;
        
        // Fazer cópia da lista para evitar ConcurrentModificationException
        List<Player> viewersCopy = new ArrayList<>(server.getViewers());
        
        // Despawn para todos os viewers
        viewersCopy.forEach(server::despawnTo);
        
        // Remover do mapa
        holograms.remove(server.getId());
    }

    public void removeClient(HologramClient client) {
        client.despawnTo(client.getReceiver());

        holograms.remove(client.getId());
    }

    public void removeClients(Player player) {
        for (Hologram hologram : holograms.values()) {
            if (!(hologram instanceof HologramClient)) continue;

            HologramClient client = (HologramClient) hologram;

            if (client.getReceiver().equals(player)) {
                client.despawnTo(player);

                holograms.remove(client.getId());
            }
        }
    }

    public HologramClient spawnClient(Player receiver, String tag, Location location) {
        int id = Integer.parseInt(StringUtil.generateNumberCode(5));

        HologramClient client = new HologramClient(receiver, tag, location);

        client.setId(id);

        holograms.put(id, client);

        return client;
    }

    public HologramClient spawnClient(Player receiver, String tag, Location location, long expiresAt) {
        int id = Integer.parseInt(StringUtil.generateNumberCode(5));

        HologramClient client = new HologramClient(receiver, tag, location, expiresAt);

        client.setId(id);

        holograms.put(id, client);

        return client;
    }

    public HologramServer spawnServer(String tag, Location location) {
        int id = Integer.parseInt(StringUtil.generateNumberCode(5));

        HologramServer server = new HologramServer(tag, location);

        server.setId(id);

        holograms.put(id, server);

        return server;
    }

    public HologramServer spawnServer(String tag, Location location, long expiresAt) {
        int id = Integer.parseInt(StringUtil.generateNumberCode(5));

        HologramServer server = new HologramServer(tag, location, expiresAt);

        server.setId(id);

        holograms.put(id, server);

        return server;
    }

}
