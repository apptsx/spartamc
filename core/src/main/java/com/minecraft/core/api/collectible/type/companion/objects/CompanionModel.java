package com.minecraft.core.api.collectible.type.companion.objects;

import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.ArmorStand;

@Getter
@RequiredArgsConstructor
public class CompanionModel {

    private final String name;
    private final ArmorStand stand;
    private final RelativeLocation location;
}
