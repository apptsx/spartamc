package com.minecraft.lobby.architect.list.main.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.data.list.api.WoolsPlacedData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WoolsPlacedLeaderboard {

    private static WoolsPlacedLeaderboard activeLeaderboard;

    private final transient HologramManager manager = BukkitCore.getManager().getHologram();
    private final Location location;
    private final String hologramId;
    private HologramServer hologram;

    public WoolsPlacedLeaderboard(Location location) {
        this(location, "top_wools_leaderboard");
    }

    public WoolsPlacedLeaderboard(Location location, String hologramId) {
        this.location = location;
        this.hologramId = hologramId;
        this.hologram = manager.getServer(hologramId);

        if (this.hologram == null) {
            this.hologram = manager.spawnServer(hologramId, location);
        }

        activeLeaderboard = this;
        handle();
    }

    public static WoolsPlacedLeaderboard getActiveLeaderboard() {
        return activeLeaderboard;
    }

    public void handle() {
        if (hologram == null) return;

        List<Map.Entry<UUID, Long>> topWools = WoolsPlacedData.getInstance().getTopWools(10);

        List<String> lines = new ArrayList<>();
        lines.add("§e§lTOP 10");
        lines.add("§6§lBLOCOS");
        lines.add("");

        for (int i = 0; i < Math.min(10, topWools.size()); i++) {
            Map.Entry<UUID, Long> entry = topWools.get(i);
            UUID playerId = entry.getKey();
            long count = entry.getValue();

            Account account = Core.getAccountData().of(playerId);
            String playerName = account != null ? account.getFormattedDisplayName() : "§7Desconhecido";
            String clanTag = "";

            if (account != null) {
                try {
                    if (account.hasClan()) {
                        clanTag = " " + account.getClanTag();
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            }

            lines.add(String.format("§e%s. %s%s §7- §b%s",
                    (i + 1), playerName, clanTag, count));
        }

        for (int i = topWools.size(); i < 10; i++) {
            lines.add("§e" + (i + 1) + ". §7Ninguém");
        }

        lines.add("");
        lines.add("§6§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        hologram.setText(lines);
    }

    public void update() {
        handle();
    }
}
