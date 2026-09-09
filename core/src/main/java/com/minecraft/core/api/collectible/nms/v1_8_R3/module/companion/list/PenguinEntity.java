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

public class PenguinEntity extends CompanionSlime {

    public PenguinEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel head = this.buildPart("head", new RelativeLocation(0.02, 0.0, 0.1), true);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDNjNTdmYWNiYjNhNGRiN2ZkNTViNWMwZGM3ZDE5YzE5Y2IwODEzYzc0OGNjYzk3MTBjNzE0NzI3NTUxZjViOSJ9fX0="));

        CompanionModel r_wing = this.buildPart("r_wing", new RelativeLocation(-0.65, -1.0, 0.35, 90.0F), false);
        r_wing.getStand().setRightArmPose(MathUtils.angle(-45, 180, 0));
        r_wing.getStand().setItemInHand(new Item(Material.FLINT));

        CompanionModel r_leg = this.buildPart("r_leg", new RelativeLocation(-0.3, 0.0, 0.5), true);
        r_leg.getStand().setItemInHand(new Item(Material.STONE_SLAB2));

        CompanionModel l_wing = this.buildPart("l_wing", new RelativeLocation(-0.65, 1.0, 0.4, 90.0F), false);
        l_wing.getStand().setRightArmPose(MathUtils.angle(-45, 0, 0));
        l_wing.getStand().setItemInHand(new Item(Material.FLINT));

        CompanionModel l_leg = this.buildPart("l_leg", new RelativeLocation(-0.3, 0.42, 0.5), true);
        l_leg.getStand().setItemInHand(new Item(Material.STONE_SLAB2));

        CompanionModel body = this.buildPart("body", new RelativeLocation(-1.3, 0.0, 0.1), false);
        body.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("ewogICJ0aW1lc3RhbXAiIDogMTcyNjYxNjMyMTUyNywKICAicHJvZmlsZUlkIiA6ICIyYThjZTE5ZWY3Mjg0NTBjYjY2YmEyNzNhNzVmNjI1MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJYRDROVDNfNjY2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk3ZmQ5ZDBlN2YyNGRmN2I1ODFlYjFjMTA2MmM0ODI5YmIzZDg3OThiNzM2Y2Q1MTgwNjljYjVhMzIzMTgzZTkiCiAgICB9CiAgfQp9"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
