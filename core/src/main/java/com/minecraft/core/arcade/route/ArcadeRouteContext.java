package com.minecraft.core.arcade.route;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@Builder
@ToString
public class ArcadeRouteContext {

    private final ArcadeCategory arcade;

    private final Slot slot;
    private final Type type;

    @Builder.Default
    private Join join = Join.PLAYER;

    @Builder.Default
    private ArcadeState state = ArcadeState.ALIVE;

    private final int roomId, mapId, maxPlayers;

    private int serverId;

    @Builder.Default
    private List<UUID> link = new ArrayList<>();

    private String arenaIdentifier, teamId;

    public boolean hasMap() {
        return mapId > 0;
    }

    public boolean isValid() {
        return arcade != null && arcade.getServer().isArcade();
    }

    public boolean isLinked() {
        return !link.isEmpty();
    }

    public boolean isSlot(Slot slot) {
        return this.slot.equals(slot);
    }
}
