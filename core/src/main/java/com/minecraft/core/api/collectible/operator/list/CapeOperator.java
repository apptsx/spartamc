package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class CapeOperator extends CollectibleOperator {

    public CapeOperator(Player host, CapeCollectible collectible) {
        super(host, CollectibleCategory.CAPES);

        handle(collectible);
    }

    @Override
    public void handle(Collectible collectible) {
        CapeCollectible cape = (CapeCollectible) collectible;

        Player player = getHost();
        if (player == null || !player.isOnline()) {
            return;
        }

        try {
            Class<?> engineClass = Class.forName("com.minecraft.core.bukkit.api.shape.manager.CollectableEngine");
            Method getSingleton = engineClass.getMethod("getSingleton");
            Object engine = getSingleton.invoke(null);

            Method startRunnable = engineClass.getMethod("startRunnable", Player.class, String.class, float.class);
            startRunnable.invoke(engine, player, cape.getResourceName(), cape.getRatio());

            setCollectible(cape);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        Player player = getHost();
        if (player == null) return;

        try {
            Class<?> engineClass = Class.forName("com.minecraft.core.bukkit.api.shape.manager.CollectableEngine");
            Method getSingleton = engineClass.getMethod("getSingleton");
            Object engine = getSingleton.invoke(null);

            Method stopRunnable = engineClass.getMethod("stopRunnable", Player.class);
            stopRunnable.invoke(engine, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}