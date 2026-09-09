package com.minecraft.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.leaderboard.period.LeaderboardPeriod;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BedPeriodSelectorHologram extends LeaderboardHologram {
    private final List<String> periods = Arrays.asList("MENSAL", "SEMANAL", "DIARIO", "TOTAL");
    private final List<LeaderboardHologram> listeners;
    private int index = 0;

    public BedPeriodSelectorHologram(Player player, Location location, List<LeaderboardHologram> listeners) {
        super("leaderboard_period_selector", player, location, 8);

        this.listeners = listeners;
    }

    @Override
    public void handle() {
        HologramClient selector = getHologram();

        selector.writeProperty("page", 0);
        selector.writeProperty("index", index);

        buildText(selector);

        applyPeriodToListeners(periods.get(index));

        selector.setTouch((player, touch) -> {
            if (touch == Touch.RIGHT) {
                index = (index - 1 + periods.size()) % periods.size();
            } else {
                index = (index + 1) % periods.size();
            }

            selector.writeProperty("index", index);

            buildText(selector);

            applyPeriodToListeners(periods.get(index));
        });
    }

    private void buildText(HologramClient selector) {
        List<String> lines = new ArrayList<>();
        lines.add("§b§lPERÍODO");
        lines.add("");

        for (int i = periods.size() - 1; i >= 0; i--) {
            String period = periods.get(i);
            boolean selected = i == index;
            String formattedPeriod = period.substring(0, 1).toUpperCase() + period.substring(1).toLowerCase();
            lines.add((selected ? "§a" : "§7") + formattedPeriod);
        }
        selector.setSpaciousText(lines);
    }

    private void applyPeriodToListeners(String periodType) {
        LeaderboardPeriod period = null;
        
        if (!periodType.equalsIgnoreCase("Total")) {
            String upperPeriod = periodType.toUpperCase();
            // Aceita tanto "DIARIO" quanto "DIÁRIO"
            if (upperPeriod.equals("DIARIO") || upperPeriod.equals("DIÁRIO")) {
                period = LeaderboardPeriod.DAY;
            } else if (upperPeriod.equals("SEMANAL")) {
                period = LeaderboardPeriod.WEEK;
            } else if (upperPeriod.equals("MENSAL")) {
                period = LeaderboardPeriod.MONTH;
            }
        }
        
        for (LeaderboardHologram lb : listeners) {
            HologramClient client = lb.getHologram();
            client.writeProperty("page", 0);
            if (period != null) {
                client.writeProperty("period", period);
            } else {
                client.removeProperty("period");
            }
            lb.handle();
        }
    }
}

