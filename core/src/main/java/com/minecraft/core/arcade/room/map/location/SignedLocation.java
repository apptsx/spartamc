package com.minecraft.core.arcade.room.map.location;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignedLocation {

    private final String name;
    private final SyntheticLocation synthetic;
}
