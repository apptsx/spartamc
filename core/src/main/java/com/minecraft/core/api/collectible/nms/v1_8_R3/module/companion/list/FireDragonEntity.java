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

public class FireDragonEntity extends CompanionSlime {

    public FireDragonEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel l_leg_b = this.buildPart("l_leg_b", new RelativeLocation(1.29, 0.42, -0.3), true);
        l_leg_b.getStand().setItemInHand(Item.of(Material.CARPET, 14));

        CompanionModel l_horn = this.buildPart("l_horn", new RelativeLocation(1.7, 0.275, 0.5), true);
        l_horn.getStand().setHeadPose(MathUtils.angle(90, 90, 180));
        l_horn.getStand().setHelmet(Item.of(Material.CARPET, 14));

        CompanionModel l_wing6 = this.buildPart("l_wing6", new RelativeLocation(1.4, 0.8, 0.0), false);
        l_wing6.getStand().setHeadPose(MathUtils.angle(90, 90, 163));
        l_wing6.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_horn = this.buildPart("r_horn", new RelativeLocation(1.7, -0.25, 0.5), true);
        r_horn.getStand().setHeadPose(MathUtils.angle(90, 90, 180));
        r_horn.getStand().setHelmet(Item.of(Material.CARPET, 14));

        CompanionModel body2 = this.buildPart("body2", new RelativeLocation(0.3, 0.0, -0.35), false);
        body2.getStand().setHelmet(Item.of(Material.WOOL, 14));

        CompanionModel l_wing1 = this.buildPart("l_wing1", new RelativeLocation(0.7, -0.15, 0.0), false);
        l_wing1.getStand().setHeadPose(MathUtils.angle(90, 70, 150));
        l_wing1.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel back = this.buildPart("back", new RelativeLocation(1.45, 0.0, -0.4), true);
        back.getStand().setHeadPose(MathUtils.angle(90, 90, 180));
        back.getStand().setHelmet(Item.of(Material.CARPET, 14));

        CompanionModel body = this.buildPart("body", new RelativeLocation(0.3, 0.0, 0.2), false);
        body.getStand().setHelmet(Item.of(Material.WOOL, 14));

        CompanionModel l_wing5 = this.buildPart("l_wing5", new RelativeLocation(0.75, -0.15, 0.0), false);
        l_wing5.getStand().setHeadPose(MathUtils.angle(90, 90, 149));
        l_wing5.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel l_wing4 = this.buildPart("l_wing4", new RelativeLocation(1.4, 0.65, 0.0), false);
        l_wing4.getStand().setHeadPose(MathUtils.angle(90, 110, 165));
        l_wing4.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel l_wing3 = this.buildPart("l_wing3", new RelativeLocation(1.4, 0.65, 0.0), false);
        l_wing3.getStand().setHeadPose(MathUtils.angle(90, 70, 165));
        l_wing3.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel l_wing2 = this.buildPart("l_wing2", new RelativeLocation(0.7, -0.15, 0.0), false);
        l_wing2.getStand().setHeadPose(MathUtils.angle(90, 110, 150));
        l_wing2.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel head = this.buildPart("head", new RelativeLocation(0.4, 0.0, 0.5), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIzNmQ3OWJhNDA3MDUzYzI1MWJkNGFhOTEzYTVlMjgxY2NkMjRkOGQ5ZWJlOTNkMjhiYjYyZjE0MGFkYzUifX19"));

        CompanionModel r_wing1 = this.buildPart("r_wing1", new RelativeLocation(0.7, 0.15, 0.0), false);
        r_wing1.getStand().setHeadPose(MathUtils.angle(-90, -70, 30));
        r_wing1.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_wing3 = this.buildPart("r_wing3", new RelativeLocation(1.4, -0.65, 0.0), false);
        r_wing3.getStand().setHeadPose(MathUtils.angle(-90, -70, 15));
        r_wing3.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_wing2 = this.buildPart("r_wing2", new RelativeLocation(0.7, 0.15, 0.0), false);
        r_wing2.getStand().setHeadPose(MathUtils.angle(-90, -110, 30));
        r_wing2.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_wing5 = this.buildPart("r_wing5", new RelativeLocation(0.75, 0.15, 0.0), false);
        r_wing5.getStand().setHeadPose(MathUtils.angle(-90, -90, 31));
        r_wing5.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_wing4 = this.buildPart("r_wing4", new RelativeLocation(1.4, -0.65, 0.0), false);
        r_wing4.getStand().setHeadPose(MathUtils.angle(-90, -110, 15));
        r_wing4.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_wing6 = this.buildPart("r_wing6", new RelativeLocation(1.4, -0.8, 0.0), false);
        r_wing6.getStand().setHeadPose(MathUtils.angle(-90, -90, 17));
        r_wing6.getStand().setHelmet(Item.of(Material.BANNER, 1));

        CompanionModel r_leg_b = this.buildPart("r_leg_b", new RelativeLocation(1.29, 0.0, -0.3), true);
        r_leg_b.getStand().setItemInHand(Item.of(Material.CARPET, 14));

        CompanionModel l_leg = this.buildPart("l_leg", new RelativeLocation(1.29, 0.42, 0.4), true);
        l_leg.getStand().setItemInHand(Item.of(Material.CARPET, 14));

        CompanionModel tail1 = this.buildPart("tail1", new RelativeLocation(1.0, 0.0, -0.6), true);
        tail1.getStand().setHelmet(Item.of(Material.WOOL, 14));

        CompanionModel r_leg = this.buildPart("r_leg", new RelativeLocation(1.29, 0.0, 0.4), true);
        r_leg.getStand().setItemInHand(Item.of(Material.CARPET, 14));

        CompanionModel tail2 = this.buildPart("tail2", new RelativeLocation(0.9, 0.0, -0.8), true);
        tail2.getStand().setHelmet(Item.of(Material.WOOL, 14));

        CompanionModel tail3 = this.buildPart("tail3", new RelativeLocation(0.8, 0.0, -1.0), true);
        tail3.getStand().setHelmet(Item.of(Material.WOOL, 14));

        CompanionModel tail4 = this.buildPart("tail4", new RelativeLocation(0.7, 0.0, -1.2), true);
        tail4.getStand().setHelmet(Item.of(Material.WOOL, 14));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(12).getStand())).setCompanionName(name);
    }
}
