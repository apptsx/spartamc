package com.minecraft.core.bukkit.api.hologram.leaderboard;

import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.touch.TouchHandler;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class LeaderboardHologram {

    private final transient HologramManager manager = BukkitCore.getManager().getHologram();

    private final String name;
    private final Player host;

    private final Location location;

    private final int maxLines;

    private HologramClient hologram;

    private long nextUpdate;

    public LeaderboardHologram(String name, Player host, Location location, int maxLines) {
        this.name = name;
        this.host = host;

        this.location = location;

        this.maxLines = maxLines;

        String tag = "leaderboard_" + name.toLowerCase();
        HologramClient existing = manager.getClient(host, tag);
        
        if (existing != null) {
            // Reutiliza o holograma existente
            this.hologram = existing;
        } else {
            // Cria um novo holograma apenas se não existir
            this.hologram = manager.spawnClient(host, tag, location);
        }

        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
    }

    public LeaderboardHologram(String name, Player host, Room arena, int maxLines) {
        this.name = name;
        this.host = host;

        this.location = arena.getLocation("leaderboard_" + name.toLowerCase());

        this.maxLines = maxLines;

        String tag = "leaderboard_" + name.toLowerCase();
        HologramClient existing = manager.getClient(host, tag);
        
        if (existing != null) {
            // Reutiliza o holograma existente
            this.hologram = existing;
        } else {
            // Cria um novo holograma apenas se não existir
            this.hologram = manager.spawnClient(host, tag, location);
        }

        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
    }

    public abstract void handle();

    public void update() {
        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

        handle();
    }

    public boolean hasUpdate() {
        return nextUpdate <= System.currentTimeMillis();
    }

    protected void setupNavigation() {
        if (hologram == null) return;
        
        hologram.setTouch(new TouchHandler() {
            @Override
            public void handle(Player player, Touch touch) {
                if (touch == Touch.LEFT) {
                    // Hit = voltar página
                    onPreviousPage();
                } else if (touch == Touch.RIGHT) {
                    // Place = avançar página
                    onNextPage();
                }
            }
        });
    }

    protected void onPreviousPage() {
        // Subclasses devem sobrescrever
    }

    protected void onNextPage() {
        // Subclasses devem sobrescrever
    }
}
