package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.util.math.MathUtils;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

public class DuckEntity extends CompanionSlime {

    public DuckEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel body = this.buildPart("body", new RelativeLocation(-0.149, 0, 0.2), true);
        body.getStand().setHeadPose(MathUtils.angle(180, 0, 0));
        body.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdjY2UwZjdjZDkxYjljMTYyODBiMDliMTJlNzk4Mjk0N2JhMTJhMzhlMzdkMDJhMDQ3ZDEwNTNjMzIzOWRkYyJ9fX0="));

        CompanionModel head = this.buildPart("head", new RelativeLocation(-0.2, 0, 0.5), true);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzUyMDcxN2FmZjJhNjYwYjI5NDExYzAxNGI4ODkxOGI3MmQxMWQwODUzYWMyYzZmZjRlZWUyODg4ZTMzYTcifX19"));

        CompanionModel leftLeg = this.buildPart("leftLeg", new RelativeLocation(-0.3, 0.42, 0.3), true);
        leftLeg.getStand().setItemInHand(new Item(Material.STONE_SLAB2));

        CompanionModel rightLeg = this.buildPart("rightLeg", new RelativeLocation(-0.3, 0, 0.3), true);
        rightLeg.getStand().setItemInHand(new Item(Material.STONE_SLAB2));

        CompanionModel leftWing = this.buildPart("leftWing", new RelativeLocation(-1.2, 1.1, 0.3), false);
        leftWing.getStand().setRightArmPose(MathUtils.angle(-90, 90, 0));
        leftWing.getStand().setItemInHand(Item.of(Material.GOLD_NUGGET));

        CompanionModel rightWing = this.buildPart("rightWing", new RelativeLocation(-1.2, 0.65, 0.3), false);
        rightWing.getStand().setRightArmPose(MathUtils.angle(-90, 90, 0));
        rightWing.getStand().setItemInHand(Item.of(Material.GOLD_NUGGET));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(1).getStand())).setCompanionName(name);
    }
}
