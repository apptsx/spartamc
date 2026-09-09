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

public class FoxEntity extends CompanionSlime {

    public FoxEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel kafa = this.buildPart("kafa", new RelativeLocation(-0.13, 0, 0), true);

        kafa.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("192f773f46ef5b2ecce7ec352a7a973f9c4738dfd7d337a9c01a041fe4e568d0"));

        CompanionModel foot = this.buildPart("foot", new RelativeLocation(-85, 0.65, -0.6), false);

        foot.getStand().setRightArmPose(MathUtils.angle(-45, 45, 0));
        foot.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("65cfe934afcdc95f74d897eb73550cadb8c67892f3a3d1b14b43e859493c5f55"));

        CompanionModel down_foot = this.buildPart("down_foot", new RelativeLocation(-0.85, 0.65, -1.3), false);

        down_foot.getStand().setRightArmPose(MathUtils.angle(-45, 45, 0));
        down_foot.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("812f6d926a0512200b81d6faebe11533f4c38e9ac03203fe5e4f237c3015c94a"));

        CompanionModel left_foot = this.buildPart("left_foot", new RelativeLocation(-85, 1, -0.6), false);

        left_foot.getStand().setRightArmPose(MathUtils.angle(-45, 45, 0));
        left_foot.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("3c321587834b7003af8d2bd649333d06ef6d46509f89d5c5f128a43b933f0217"));

        CompanionModel down_left_foot = this.buildPart("down_left_foot", new RelativeLocation(-0.85, 1, -1.3), false);

        down_left_foot.getStand().setRightArmPose(MathUtils.angle(-45, 45, 0));
        down_left_foot.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("e09090502a0770e9f710e4b12fc0e0d7267a650b692017b958eed95c60d13ccc"));

        CompanionModel kuyruk = this.buildPart("kuyruk", new RelativeLocation(-1.13, 0.75, -0.7), false);

        kuyruk.getStand().setRightArmPose(MathUtils.angle(0, -22.5, 90));

        kuyruk.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("854dc084c0f7c79b2ce4dc26235a7e6fbe00783ca1cab9de17f00eff68703ee"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
