package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.bukkit.api.sidebar.row.SidebarRow;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerSidebarUpdateEvent extends EventHandler {

    private final Player player;
    private final SidebarRow row;
}
