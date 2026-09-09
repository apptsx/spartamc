package com.minecraft.core.api.clan.metadata;

import com.minecraft.core.api.clan.member.ClanMember;
import com.minecraft.core.api.clan.metadata.emblem.ClanEmblem;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ClanMetadata {

    private final Map<UUID, ClanMember> members = new HashMap<>();
    private final Set<ClanEmblem> emblems = new HashSet<>(Collections.singletonList(ClanEmblem.DEFAULT));

    private int power;

    private final long createdAt = System.currentTimeMillis();
}
