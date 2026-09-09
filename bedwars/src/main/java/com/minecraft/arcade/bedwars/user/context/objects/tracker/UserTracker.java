package com.minecraft.arcade.bedwars.user.context.objects.tracker;

import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserTracker {

    private Team team;
    private UUID tracking;

    private boolean active;

    public void handle(Team team, UUID tracking) {
        this.team = team;
        this.tracking = tracking;
    }

    public void cancel() {
        this.team = null;
        this.tracking = null;

        this.active = false;
    }

    public boolean isValid() {
        return active && team != null && tracking != null && User.has(tracking);
    }
}
