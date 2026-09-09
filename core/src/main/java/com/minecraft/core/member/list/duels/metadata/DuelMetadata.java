package com.minecraft.core.member.list.duels.metadata;

import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DuelMetadata {

    private boolean spectatorEnabled = true;

    private final Map<DuelCollectibleType, String> collectibles = new HashMap<>();

}
