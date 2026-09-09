package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

public class GiraffeEntity extends CompanionSlime {

    public GiraffeEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel front_Left = this.buildPart("front_Left", new RelativeLocation(-0.7, 0.3, 0.0), true);
        front_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel front_Rightup = this.buildPart("front_Rightup", new RelativeLocation(-0.3, -0.3, 0.0), true);
        front_Rightup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel front_Right = this.buildPart("front_Right", new RelativeLocation(-0.7, -0.3, 0.0), true);
        front_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel neck = this.buildPart("neck", new RelativeLocation(0.6, 0.0, 0.0), true);
        neck.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel back_Leftup = this.buildPart("back_Leftup", new RelativeLocation(-0.3, 0.3, -1.0), true);
        back_Leftup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel neck2 = this.buildPart("neck2", new RelativeLocation(1.0, 0.0, 0.0), true);
        neck2.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel back_Right = this.buildPart("back_Right", new RelativeLocation(-0.7, -0.3, -1.0), true);
        back_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel head = this.buildPart("head", new RelativeLocation(0.6, 0.0, 0.245), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZkZDdiYmNlYWMxMzQ2NDE4Njg0YjFmYTgzMzc3ZDU3OTZjZjMxNTRkYjI5OWYyZDk5OTFiOTZlM2MzZDk5In19fQ=="));

        CompanionModel middle_Belly = this.buildPart("middle_Belly", new RelativeLocation(-0.65, 0.25, -1.0), false);
        middle_Belly.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel middle_Belly3 = this.buildPart("middle_Belly3", new RelativeLocation(-0.65, 0.25, -0.55), false);
        middle_Belly3.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel middle_Belly2 = this.buildPart("middle_Belly2", new RelativeLocation(-0.65, -0.25, -1.0), false);
        middle_Belly2.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel middle_Belly6 = this.buildPart("middle_Belly6", new RelativeLocation(-0.65, -0.25, -0.05), false);
        middle_Belly6.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel middle_Belly5 = this.buildPart("middle_Belly5", new RelativeLocation(-0.65, 0.25, -0.05), false);
        middle_Belly5.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel middle_Belly4 = this.buildPart("middle_Belly4", new RelativeLocation(-0.65, -0.25, -0.55), false);
        middle_Belly4.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel back_Left = this.buildPart("back_Left", new RelativeLocation(-0.7, 0.3, -1.0), true);
        back_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel back_Rightup = this.buildPart("back_Rightup", new RelativeLocation(-0.3, -0.3, -1.0), true);
        back_Rightup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        CompanionModel front_Leftup = this.buildPart("front_Leftup", new RelativeLocation(-0.3, 0.3, 0.0), true);
        front_Leftup.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNjQ1NWMzZGQxMzQzN2Q4ODM0MTBiZDJlNzliMzRlNmRhNjY1ZTRlN2U2NjhlZjBhZjIyOWZiNWE5NjAifX19"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(7).getStand())).setCompanionName(name);
    }
}
