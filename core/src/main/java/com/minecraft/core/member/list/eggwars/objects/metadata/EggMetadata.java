package com.minecraft.core.member.list.eggwars.objects.metadata;

import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.eggwars.objects.menu.EggMenuMetadata;
import com.minecraft.core.member.list.eggwars.objects.metadata.objects.stats.EggStats;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EggMetadata {

    private ProfileType profile = ProfileType.NORMAL;
    
    private boolean showRank = true;
    private boolean showBar = true;

    private final List<EggStats> statsList = new ArrayList<>();
    private List<EggMenuMetadata> menus = new ArrayList<>();
}

