package com.minecraft.core.arcade.room.team.preset;

import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class TeamPreset extends Team {

    private Location bedLocation, portalLocation;

    private int score;

    public TeamPreset(TeamPresetType type, Room arena, Location base, int maxPlayers) {
        super(type.getName(), type.getId(), type.getColor(), type.getRgb(), arena, base, maxPlayers);

        String name = type.name().toLowerCase();

        if (arena.hasLocation(name + "_bed"))
            this.bedLocation = arena.getLocation(name + "_bed");

        if (arena.hasLocation(name + "_portal"))
            this.portalLocation = arena.getLocation(name + "_portal");
    }

    public boolean hasBed() {
        return bedLocation != null && bedLocation.getBlock().getType().name().contains("BED");
    }

    public boolean hasPortal() {
        return portalLocation != null && portalLocation.getWorld().getName().equalsIgnoreCase(getArena().getWorld().getName());
    }

    public boolean isYourBed(Location location) {
        return hasBed() && location.distance(bedLocation) <= 1 && location.getBlock().getType().name().contains("BED");
    }

    public String getTypeName() {
        return TeamPresetType.of(getColor()).name();
    }

    public String getBrackets() {
        return getColor() + "[" + getId() + "]";
    }

    public String getBracketsName() {
        return getColor() + "[" + getId() + "] §f" + getName();
    }

    public String getColoredId() {
        return getColor() + getId();
    }

    public String getDisplay() {
        return getColoredId() + " " + getName();
    }

    public String getBedName(int aliveUsers) {
        return hasBed() ? "§a✓" : aliveUsers > 0 ? "§a" + aliveUsers : "§c✘";
    }
}
