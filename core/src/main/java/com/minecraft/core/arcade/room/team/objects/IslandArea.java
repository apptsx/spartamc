package com.minecraft.core.arcade.room.team.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class IslandArea {
    private final int frontAndBack, up, sides;

    private boolean axisX;
}
