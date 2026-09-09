package com.minecraft.core.bukkit.api.sidebar.row;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.scoreboard.Score;

@Getter
@Setter
@RequiredArgsConstructor
public class SidebarRow {

    private final String field;
    private String prefix;
    private String suffix;

    private Score score;

    private int slot;

    public SidebarRow(String field, String prefix) {
        this.field = field;

        this.prefix = prefix;
        this.suffix = "";
    }
}
