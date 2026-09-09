package com.minecraft.core.member.list.bedwars.objects.metadata;

import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BedMetadata {

    private ProfileType profile = ProfileType.NORMAL;
    private ShareType share = ShareType.DEFAULT;

    private boolean tntTimerEnabled = true, showRank = true, showBar = true;

    private final List<BedStats> statsList = new ArrayList<>();
    private List<BedMenuMetadata> menus = new ArrayList<>();

    private final Map<BedCollectibleType, String> collectibles = new HashMap<>();
    
    // Sistema de moedas e habilidades
    private int coins = 0; // Moedas do jogador
    private final Set<String> purchasedAbilities = new HashSet<>(); // Nomes das habilidades compradas
    private final Set<String> selectedAbilities = new HashSet<>(); // Habilidades selecionadas para usar
}
