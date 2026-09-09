package com.minecraft.core.arcade.room.event;

import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public class ArenaPhaseEvent extends Event {

    private final Room arena;
    private final RoomPhase phase;

    public boolean isPhase(RoomPhase phase) {
        return this.phase.equals(phase);
    }

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
