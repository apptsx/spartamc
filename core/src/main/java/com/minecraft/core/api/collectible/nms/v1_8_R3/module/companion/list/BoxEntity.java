package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

public class BoxEntity extends CompanionSlime {

    public BoxEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel top = this.buildPart("top", new RelativeLocation(-0.7, 0.0, 0.0), false);

        top.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5NWUyNWIyOGVjMmVlNDJkNTc4NDZjODM3MTNlMWVkMjk4YTNkMDZjMDMyMWI3YmRjOWVhM2EzNGJiOWU5NyJ9fX0="));

        CompanionModel below = this.buildPart("below", new RelativeLocation(-1.3, 0.0, 0.0), false);

        below.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5NWUyNWIyOGVjMmVlNDJkNTc4NDZjODM3MTNlMWVkMjk4YTNkMDZjMDMyMWI3YmRjOWVhM2EzNGJiOWU5NyJ9fX0="));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(1).getStand())).setCompanionName(name);
    }
}
