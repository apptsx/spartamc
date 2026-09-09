package com.minecraft.core.arcade.room.custom;

import com.minecraft.core.arcade.room.map.Map;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoomCustom {

    private final UUID author;

    private List<Map> maps, playedMaps;

    private int teamSize;

    public RoomCustom(UUID author, List<Map> maps, int teamSize) {
        this.author = author;

        this.maps = maps;
        this.playedMaps = new ArrayList<>();

        this.teamSize = teamSize;
    }

    public boolean isAuthor(UUID author) {
        return this.author.equals(author);
    }

    public boolean isMapAlreadyPlayed(Map map) {
        return playedMaps.contains(map);
    }

    public boolean isMapSelected(Map map) {
        return maps.contains(map);
    }
}
