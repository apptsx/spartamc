package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class EmotionOperator extends CollectibleOperator {

    private Timer task;
    private EmotionCollectible.EmotionAnimation animation;

    public EmotionOperator(Player host, EmotionCollectible collectible) {
        super(host, CollectibleCategory.EMOTION);

        this.handle(collectible);

        setCategoriesWithConflict(Collections.singletonList(CollectibleCategory.HAT));
    }

    @Override
    public void handle(Collectible collectible) {
        EmotionCollectible emotion = (EmotionCollectible) collectible;

        this.task = new Timer();
        this.animation = new EmotionCollectible.EmotionAnimation(emotion);

        this.task.schedule(new TimerTask() {
            @Override
            public void run() {
                String id = animation.next();

                if (id != null)
                    getHost().getInventory().setHelmet(new Item(Material.SKULL_ITEM, 1, 3).skullByUrl(id));
            }
        }, 0, 20L * emotion.getTicksPerFrame());

        setCollectible(collectible);
    }

    @Override
    public void cancel() {
        if (task != null)
            task.cancel();

        getHost().getInventory().setHelmet(null);

        this.task = null;
        this.animation = null;
    }
}
