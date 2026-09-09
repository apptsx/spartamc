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

public class PandaEntity extends CompanionSlime {

    public PandaEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel body = this.buildPart("body", new RelativeLocation(-1, 0, 0.3), false);

        body.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3YjY4ZWQwMjE2MzJmNDA4ZmMyMjNlZjc5NTdjMjQ3ODZhZTUwOWE4NGU2ZjE4YTM3MWE1NWMzZDhjZjkwOSJ9fX0="));

        CompanionModel backLeft = this.buildPart("backLeft", new RelativeLocation(-1, 0.2, -1), true);
        backLeft.getStand().setRightArmPose(MathUtils.angle(190, 500, 13));
        backLeft.getStand().setItemInHand(new Item(Material.COAL_BLOCK));

        CompanionModel backRight = this.buildPart("backRight", new RelativeLocation(-1, -0.2, -1), true);
        backRight.getStand().setRightArmPose(MathUtils.angle(190, 500, 13));
        backRight.getStand().setItemInHand(new Item(Material.COAL_BLOCK));

        CompanionModel frontLeft = this.buildPart("frontLeft", new RelativeLocation(-1, 0.2, 0), true);
        frontLeft.getStand().setRightArmPose(MathUtils.angle(190, 500, 13));
        frontLeft.getStand().setItemInHand(new Item(Material.COAL_BLOCK));

        CompanionModel frontRight = this.buildPart("frontRight", new RelativeLocation(-1, -0.2, 0), true);
        frontRight.getStand().setRightArmPose(MathUtils.angle(190, 500, 13));
        frontRight.getStand().setItemInHand(new Item(Material.COAL_BLOCK));

        CompanionModel backSlab = this.buildPart("backSlab", new RelativeLocation(-.95, 0, -.9325), false);
        backSlab.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        backSlab.getStand().setHelmet(Item.of(Material.STEP, 7));

        CompanionModel frontSlab = this.buildPart("frontSlab", new RelativeLocation(-.95, 0, 0), false);
        frontSlab.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        frontSlab.getStand().setHelmet(Item.of(Material.STEP, 7));

        CompanionModel middleBelly = this.buildPart("middleBelly", new RelativeLocation(-.95, 0, -.625), false);
        middleBelly.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        middleBelly.getStand().setHelmet(new Item(Material.COAL_BLOCK));

        CompanionModel tail = this.buildPart("tail", new RelativeLocation(-.25, -.05, -1.15), true);

        tail.getStand().setRightArmPose(MathUtils.angle(183, 500, 13));
        tail.getStand().setItemInHand(new Item(Material.COAL_BLOCK));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
