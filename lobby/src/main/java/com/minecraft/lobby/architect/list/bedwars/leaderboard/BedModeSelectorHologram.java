package com.minecraft.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BedModeSelectorHologram extends LeaderboardHologram {
    private final List<String> modes = Arrays.asList("4V4", "3V3", "2V2", "1V1", "QUARTETOS", "TRIOS", "DUPLAS", "SOLO", "GERAL");
    private final List<LeaderboardHologram> listeners;
    private int index = 8;

    public BedModeSelectorHologram(Player player, Location location, List<LeaderboardHologram> listeners) {
        super("selector", player, location, 8);

        this.listeners = listeners;
    }

    @Override
    public void handle() {
        HologramClient selector = getHologram();

        selector.writeProperty("page", 0);
        selector.writeProperty("index", index);

        buildText(selector);

        applyModeToListeners(modes.get(index));

        selector.setTouch((player, touch) -> {
            if (touch == Touch.RIGHT) {
                index = (index - 1 + modes.size()) % modes.size();
            } else {
                index = (index + 1) % modes.size();
            }

            selector.writeProperty("index", index);

            buildText(selector);

            applyModeToListeners(modes.get(index));
        });
    }

    private void buildText(HologramClient selector) {
        List<String> lines = new ArrayList<>();
        lines.add("§b§lMODOS");
        lines.add("");

        for (int i = modes.size() - 1; i >= 0; i--) {
            String mode = modes.get(i);
            boolean selected = i == index;
            lines.add((selected ? "§a" : "§7") + toTitle(mode));
        }
        selector.setSpaciousText(lines);
    }

    private void applyModeToListeners(String mode) {
        for (LeaderboardHologram lb : listeners) {
            HologramClient client = lb.getHologram();
            client.writeProperty("mode", mode);
            lb.handle();
        }
    }

    private String toTitle(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}