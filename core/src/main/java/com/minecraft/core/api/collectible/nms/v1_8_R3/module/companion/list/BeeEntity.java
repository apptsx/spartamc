package com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

import java.util.Timer;
import java.util.TimerTask;

public class BeeEntity extends CompanionSlime {

    public BeeEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        // Cabeça da Abelha
        CompanionModel head = this.buildPart("head", new RelativeLocation(2, 0.0, 0.0), false);
        head.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI3YWI4YmZmMWU1MjcxNDhiYTc0ZjNjZDYyYjc0MWRkNjU1ZGM3ZDg3NTk5OGE2NzNlZWEwNjJhMTlmMDRmMiJ9fX0="));

        // Spawn das partes
        this.spawn();

        // Iniciar o efeito de partículas
        this.startParticleEffect();
    }

    @Override
    public void setCompanionName(String name) {
        // Opcionalmente, definir o nome acima da cabeça da abelha
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }

    private void startParticleEffect() {
        ArmorStand headStand = this.models.get(0).getStand();

        // Criar partículas amarelas quando a abelha se move
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (companion.getCollectible() == null) {
                    cancel();
                    return;
                }

                ParticleEffect.REDSTONE.display(0.3f, 1.3f, -0.5f, 0.0f, 4, headStand.getLocation());
            }
        }, 0, 40);
    }
}
