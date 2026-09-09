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

public class ChimpanzeeEntity extends CompanionSlime {

    public ChimpanzeeEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel head = this.buildPart("head", new RelativeLocation(-0.4, 0.0, 0.0), false);

        head.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQzOTI4ODFhMTNhNDJiMDhhZWI4ODhmMzk5ZTFkZWJkYWNlMDRiYzVhMGUwMWMyMjFjMGI1ZDExZDQwIn19fQ=="));

        CompanionModel front_Left = this.buildPart("front_Left", new RelativeLocation(-0.7, 0.3, 0.0), true);

        front_Left.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel front_Right = this.buildPart("front_Right", new RelativeLocation(-0.7, -0.3, 0.0), true);

        front_Right.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel front_Rightup = this.buildPart("front_Rightup", new RelativeLocation(-0.3, -0.3, 0.0), true);

        front_Rightup.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel Belly1 = this.buildPart("Belly1", new RelativeLocation(-0.95, 0.2, -0.688), false);

        Belly1.getStand().setHeadPose(MathUtils.angle(-15, 0, 0));
        Belly1.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel Belly3 = this.buildPart("Belly3", new RelativeLocation(-0.85, 0.2, -0.344), false);

        Belly3.getStand().setHeadPose(MathUtils.angle(-15, 0, 0));
        Belly3.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel back_Left = this.buildPart("back_Left", new RelativeLocation(-0.7, 0.3, -1.0), true);

        back_Left.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel Belly2 = this.buildPart("Belly2", new RelativeLocation(-0.95, -0.2, -0.688), false);

        Belly2.getStand().setHeadPose(MathUtils.angle(-15, 0, 0));
        Belly2.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel back_Right = this.buildPart("back_Right", new RelativeLocation(-0.7, -0.3, -1.0), true);

        back_Right.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel Belly4 = this.buildPart("Belly4", new RelativeLocation(-0.85, -0.2, -0.344), false);

        Belly4.getStand().setHeadPose(MathUtils.angle(-15, 0, 0));
        Belly4.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        CompanionModel front_Leftup = this.buildPart("front_Leftup", new RelativeLocation(-0.3, 0.3, 0.0), true);

        front_Leftup.getStand().setHelmet(new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NzQ0Zjk3ZDJjNGI5MDRiYTg3YzkzM2JkYzIzOTU3NDBiOWEyMzgwYTcyYjQ0MjU4MGMzOTJhY2M3Y2IifX19"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
