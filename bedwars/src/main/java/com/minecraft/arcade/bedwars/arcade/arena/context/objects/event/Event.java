package com.minecraft.arcade.bedwars.arcade.arena.context.objects.event;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.event.phase.EventPhase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {

    private EventPhase phase = EventPhase.DIAMOND_GENERATOR_II;

    private int time = 60 * 5;

    public void handle(Arena arena) {
        this.phase.getAction().execute(arena);

        if (isNotPhase(EventPhase.SUDDEN_DEATH)) {
            this.phase = EventPhase.values()[phase.ordinal() + 1];
            this.time = 60 * 5;
        }
    }

    public boolean isNotPhase(EventPhase phase) {
        return !this.phase.equals(phase);
    }
}
