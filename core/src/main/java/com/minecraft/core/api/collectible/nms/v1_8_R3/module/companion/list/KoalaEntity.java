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

public class KoalaEntity extends CompanionSlime {

    public KoalaEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel head = this.buildPart("head", new RelativeLocation(-0.8, 0.0, 0.3), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ4N2U4MjlmMmQ0ODlmZjZlZTY2ODI2MzllMzM5YTIxMjNkMzQ0OWQyN2Y4YmRhNTE1MjhjNjA3NmZiOWYyYSJ9fX0="));

        CompanionModel front_Left = this.buildPart("front_Left", new RelativeLocation(-0.7, 0.3, 0.0), true);
        front_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        CompanionModel front_Belly = this.buildPart("front_Belly", new RelativeLocation(-0.25, 0.0, -0.6), true);
        front_Belly.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        front_Belly.getStand().setHelmet(new Item(Material.SNOW_BLOCK));

        CompanionModel front_Right = this.buildPart("front_Right", new RelativeLocation(-0.7, -0.3, 0.0), true);
        front_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        CompanionModel back_Belly = this.buildPart("back_Belly", new RelativeLocation(-0.25, 0.0, -0.9), true);
        back_Belly.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        back_Belly.getStand().setHelmet(new Item(Material.SNOW_BLOCK));

        CompanionModel front_Bellyup = this.buildPart("front_Bellyup", new RelativeLocation(-0.85, 0.0, -0.5), false);
        front_Bellyup.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        front_Bellyup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        CompanionModel back_Bellyup = this.buildPart("back_Bellyup", new RelativeLocation(-0.85, 0.0, -1.0), false);
        back_Bellyup.getStand().setHeadPose(MathUtils.angle(90, 0, 0));
        back_Bellyup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        CompanionModel back_Left = this.buildPart("back_Left", new RelativeLocation(-0.7, 0.3, -1.0), true);
        back_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        CompanionModel back_Right = this.buildPart("back_Right", new RelativeLocation(-0.7, -0.3, -1.0), true);
        back_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliODVkZThkODIyNWZjYmNlMzc5N2IxM2MxM2VlNDJhY2Q3YWFhZmFkZDg2MTkxM2Q0YWJjNzczODNmYmMifX19"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
