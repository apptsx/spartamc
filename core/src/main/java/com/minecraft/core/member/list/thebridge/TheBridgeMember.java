package com.minecraft.core.member.list.thebridge;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TheBridgeMember extends Member {

    public TheBridgeMember(UUID id, String name) {
        super(id, name);

        this.addMenu(
                new MenuMetadata(ArcadeCategory.THE_BRIDGE_SOLO, Constant.DUELS_THE_BRIDGE_BASE64)
        );

        ArcadeCategory.of(ServerType.THE_BRIDGE).forEach(arcade -> this.addMultipleStats(new ArcadeStats(arcade)));
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getTheBridgeData().update(this, field);
    }
}
