package com.minecraft.core.arcade.category.builder;

import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.rule.ArcadeRule;
import com.minecraft.core.arcade.style.ArcadeStyle;
import com.minecraft.core.server.type.ServerType;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ArcadeCategoryBuilder {

    private final ServerType server;

    @Builder.Default
    private final ArcadeRule rule = ArcadeRule.NONE;
    @Builder.Default
    private final ArcadeStyle style = ArcadeStyle.NONE;

    private final String name;
    @Builder.Default
    private final String iconId = "AIR";

    @Builder.Default
    private final List<Slot> slots = Collections.singletonList(Slot.NONE);
    @Builder.Default
    private final List<String> lore = Collections.emptyList();

    @Builder.Default
    private final List<ArcadeFeature> features = Collections.emptyList();

    @Builder.Default
    private final List<Type> types = Collections.emptyList();
}
