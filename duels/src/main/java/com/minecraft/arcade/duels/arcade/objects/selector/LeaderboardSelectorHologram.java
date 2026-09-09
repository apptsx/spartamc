package com.minecraft.arcade.duels.arcade.objects.selector;

import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardSelectorHologram extends LeaderboardHologram {

    private final List<String> modes;
    private final Map<String, LeaderboardHologram> modeMap;

    private int index = 0;

    public LeaderboardSelectorHologram(Player player, Location location, Map<String, LeaderboardHologram> modeMap) {
        super("selector", player, location, Math.max(10, (modeMap != null ? modeMap.size() + 2 : 10))); // title + modes + hint

        Map<String, LeaderboardHologram> linked = new LinkedHashMap<>();
        if (modeMap != null) linked.putAll(modeMap);
        this.modeMap = Collections.unmodifiableMap(linked);
        this.modes = new ArrayList<>(linked.keySet());
    }

    @Override
    public void handle() {
        HologramClient selector = getHologram();

        selector.writeProperty("page", 0);
        selector.writeProperty("index", index);

        List<String> lines = new ArrayList<>();
        lines.add("§b§lSELECIONE UM MODO:");

        for (int i = 0; i < modes.size(); i++) {
            String mode = modes.get(i);
            boolean selected = (i == index);
            lines.add((selected ? "§a" : "§7") + capitalize(mode));
        }

        lines.add("§6§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        selector.setSpaciousText(lines);

        updateLeaderboardsVisibility(selector.getReceiver());

        selector.setTouch((player, touch) -> {
            if (touch == Touch.RIGHT) {
                index = (index - 1 + modes.size()) % modes.size();
            } else {
                index = (index + 1) % modes.size();
            }

            selector.writeProperty("index", index);

            List<String> updated = new ArrayList<>();
            updated.add("§b§lSELECIONE UM MODO:");
            for (int i = 0; i < modes.size(); i++) {
                String mode = modes.get(i);
                boolean selected = (i == index);
                updated.add((selected ? "§a" : "§7") + capitalize(mode));
            }
            updated.add("§6§lClique para trocar!");
            selector.setSpaciousText(updated);

            updateLeaderboardsVisibility(player);
        });
    }

    private void updateLeaderboardsVisibility(Player player) {
        String selected = modes.isEmpty() ? null : modes.get(index);

        for (Map.Entry<String, LeaderboardHologram> entry : modeMap.entrySet()) {
            String mode = entry.getKey();
            LeaderboardHologram lb = entry.getValue();
            if (lb == null || lb.getHologram() == null) continue;

            if (mode.equalsIgnoreCase(selected)) {
                lb.getHologram().spawnTo(player);
                lb.handle();
            } else {
                lb.getHologram().despawnTo(player);
            }
        }
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        if (input.length() == 1) return input.toUpperCase();

        String[] parts = input.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
            if (i < parts.length - 1) builder.append(' ');
        }
        return builder.toString();
    }
}