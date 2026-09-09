package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerArenaWarpEvent extends EventHandler {

    private final Player player;
    private final Room arena;
}
