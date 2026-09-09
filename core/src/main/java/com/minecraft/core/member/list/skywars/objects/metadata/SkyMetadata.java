package com.minecraft.core.member.list.skywars.objects.metadata;

import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.skywars.objects.metadata.objects.stats.SkyStats;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SkyMetadata {

    private ProfileType profile = ProfileType.NORMAL;

    private final List<SkyStats> statsList = new ArrayList<>();
}

