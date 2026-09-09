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

public class TurtleEntity extends CompanionSlime {

    public TurtleEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        CompanionModel front_Left = this.buildPart("front_Left", new RelativeLocation(-0.7, 0.3, 0.0), true);
        front_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel shell_3 = this.buildPart("shell_3", new RelativeLocation(-0.15, -0.15, -0.688), true);
        shell_3.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U5NzJmNmM1ZWQxODdiYWU4M2E0MzQ2YzllNzdhYWY3OGMyYTQ1Y2VhYzg1NDIxNGY4Y2U2NGUzMzQ3ODk3In19fQ=="));

        CompanionModel front_Right = this.buildPart("front_Right", new RelativeLocation(-0.7, -0.3, 0.0), true);
        front_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel shell_4 = this.buildPart("shell_4", new RelativeLocation(-0.15, 0.1, -0.288), true);
        shell_4.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U5NzJmNmM1ZWQxODdiYWU4M2E0MzQ2YzllNzdhYWY3OGMyYTQ1Y2VhYzg1NDIxNGY4Y2U2NGUzMzQ3ODk3In19fQ=="));

        CompanionModel shell_1 = this.buildPart("shell_1", new RelativeLocation(-0.15, -0.15, -0.288), true);
        shell_1.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U5NzJmNmM1ZWQxODdiYWU4M2E0MzQ2YzllNzdhYWY3OGMyYTQ1Y2VhYzg1NDIxNGY4Y2U2NGUzMzQ3ODk3In19fQ=="));

        CompanionModel shell_2 = this.buildPart("shell_2", new RelativeLocation(-0.15, 0.1, -0.688), true);
        shell_2.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U5NzJmNmM1ZWQxODdiYWU4M2E0MzQ2YzllNzdhYWY3OGMyYTQ1Y2VhYzg1NDIxNGY4Y2U2NGUzMzQ3ODk3In19fQ=="));

        CompanionModel tail = this.buildPart("tail", new RelativeLocation(-0.4, -0.05, -1.15), true);
        tail.getStand().setRightArmPose(MathUtils.angle(183, 500, 13));
        tail.getStand().setItemInHand(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel __middle_Belly4 = this.buildPart("__middle_Belly4", new RelativeLocation(-0.5, 0.1, -0.288), true);
        __middle_Belly4.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel back_Right = this.buildPart("back_Right", new RelativeLocation(-0.7, -0.3, -1.0), true);
        back_Right.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel head = this.buildPart("head", new RelativeLocation(-1.0, 0.0, 0.245), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTlkNjcxMjU4MmQ2MGEwMDU4ZTRmYmVlMGU4OWRkMDg5ZmJhYmI4MGYwN2U0YTA4NzQ5MDRjOTFiYzQ4ZjA4YSJ9fX0=6"));

        CompanionModel middle_Belly = this.buildPart("middle_Belly", new RelativeLocation(-0.5, -0.15, -0.688), true);
        middle_Belly.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel middle_Belly3 = this.buildPart("middle_Belly3", new RelativeLocation(-0.5, -0.15, -0.288), true);
        middle_Belly3.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel middle_Belly2 = this.buildPart("middle_Belly2", new RelativeLocation(-0.5, 0.1, -0.688), true);
        middle_Belly2.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        CompanionModel back_Left = this.buildPart("back_Left", new RelativeLocation(-0.7, 0.3, -1.0), true);
        back_Left.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlmY2YwOWMyMjk2ZjM4MjY5MWM4ZTNkYjdjYTM2ZTM4Y2M5MjQ5Yjg4OWRkZGNjOGI5MmI3ZjA0MGZkNDQifX19"));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(9).getStand())).setCompanionName(name);
    }
}
