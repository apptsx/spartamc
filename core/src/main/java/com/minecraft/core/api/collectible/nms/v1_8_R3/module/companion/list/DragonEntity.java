package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

public class DragonEntity extends CompanionSlime {

    public DragonEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        // Cabeça do Dragão
        CompanionModel head = this.buildPart("head", new RelativeLocation(0.0, 0.0, 0.0), true);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzYjNiZjdhZmJiMmFmNjg0NjgxZjU4MjNkNGZiZmI3YjRmZTg4OTAzMTk1ZDkxNzUyZGQzMGE5ZTU0MzExMCJ9fX0="));

        // Asa Esquerda
        CompanionModel leftWing = this.buildPart("left_wing", new RelativeLocation(0.0, 0.5, -0.5), true);
        leftWing.getStand().setHelmet(Item.of(Material.BANNER, 1).durability((short) 15)); // Usando um banner preto como asa

        // Asa Direita
        CompanionModel rightWing = this.buildPart("right_wing", new RelativeLocation(0.0, -0.5, -0.5), true);
        rightWing.getStand().setHelmet(Item.of(Material.BANNER, 1).durability((short) 15));

        // Corpo do Dragão
        CompanionModel body = this.buildPart("body", new RelativeLocation(-0.5, 0.0, 0.0), false);
        body.getStand().setHelmet(Item.of(Material.ENDER_STONE, 1));

        // Cauda do Dragão
        CompanionModel tail = this.buildPart("tail", new RelativeLocation(-1.0, 0.0, 0.0), false);
        tail.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA3NTE2MzZjZGYxMjhlMjUxNmY1NjZjNDMzZmRkNTIwM2Q4Nzk3YjE5NmYwZWM1MDJkZjk5ZTYyNDEzMzQ2YSJ9fX0="));

        // Spawn das partes
        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        // Define o nome acima da cabeça do dragão (opcional)
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
