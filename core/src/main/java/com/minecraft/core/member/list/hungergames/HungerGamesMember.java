package com.minecraft.core.member.list.hungergames;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class HungerGamesMember extends Member {

    public HungerGamesMember(UUID id, String name) {
        super(id, name);

        ArcadeCategory.of(ServerType.HUNGERGAMES).forEach(arcade ->
                this.addMultipleStats(new ArcadeStats(arcade)));
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getHungerGamesData().update(this, field);
    }
}