package com.minecraft.core.arcade.room.team;

import com.minecraft.core.arcade.room.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public abstract class Team {

    private final String name;
    private final String id;

    private final ChatColor color;
    private final Color rgb;

    private final Room arena;
    private final Location base;

    private final Set<UUID> members = new HashSet<>();

    private int maxPlayers;

    @Override
    public boolean equals(Object teamObj) {
        if (teamObj == null || getClass() != teamObj.getClass()) return false;

        Team team = (Team) teamObj;

        return team.getArena().equals(arena) && team.getName().equals(name) && team.getId().equals(id);
    }

    public List<Player> getPlayers() {
        return members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean isFull() {
        return members.size() >= maxPlayers;
    }

    public boolean isDead() {
        return members.isEmpty();
    }

    public String getColoredName() {
        return color + name;
    }

    public String getIdBoldName() {
        return color + "§l" + id + "§r " + name;
    }

    public String getCodeId() {
        return name.toLowerCase() + ":" + id.toLowerCase();
    }
}
