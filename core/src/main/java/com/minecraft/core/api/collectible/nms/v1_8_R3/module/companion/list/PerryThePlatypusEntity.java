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

public class PerryThePlatypusEntity extends CompanionSlime {

    public PerryThePlatypusEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel head = this.buildPart("head", new RelativeLocation(-0.95, 0.0, 0.0), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U3YzE4MWExOWYzNjdmMWFjOTQ5NWEyYmU3NjFhZDBkOTE1NzRiNzVlZDc5OGY3NjVhMWVlNmJlNDFhODcxIn19fQ=="));

        CompanionModel front_Left = this.buildPart("front_Left", new RelativeLocation(-1.0, 0.1, -0.2), true);
        front_Left.getStand().setRightArmPose(MathUtils.angle(180, 500, 25));
        front_Left.getStand().setItemInHand(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        CompanionModel front_Right = this.buildPart("front_Right", new RelativeLocation(-1.0, -0.3, -0.2), true);
        front_Right.getStand().setRightArmPose(MathUtils.angle(180, 500, 25));
        front_Right.getStand().setItemInHand(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        CompanionModel tail = this.buildPart("tail", new RelativeLocation(-0.5, 0.7, -1.75), false);
        tail.getStand().setRightArmPose(MathUtils.angle(0, -22.5, 90));
        tail.getStand().setItemInHand(Item.of(Material.BANNER, 14));

        CompanionModel body2 = this.buildPart("body2", new RelativeLocation(-1.35, 0.0, -0.75), false);
        body2.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        CompanionModel body = this.buildPart("body", new RelativeLocation(-1.35, 0.0, -0.25), false);
        body.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        CompanionModel back_Left = this.buildPart("back_Left", new RelativeLocation(-1.0, 0.1, -1.0), true);
        back_Left.getStand().setRightArmPose(MathUtils.angle(180, 500, 25));
        back_Left.getStand().setItemInHand(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        CompanionModel back_Right = this.buildPart("back_Right", new RelativeLocation(-1.0, -0.3, -1.0), true);
        back_Right.getStand().setRightArmPose(MathUtils.angle(180, 500, 25));
        back_Right.getStand().setItemInHand(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkOTI2ZmI1NzRlMjI5MzAyMjJhOWMzNTNjMGM1YjRiNDVjMTc5NjIxZDEzZWY0NzUzZThiYzk0YjEzYjcifX19"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
