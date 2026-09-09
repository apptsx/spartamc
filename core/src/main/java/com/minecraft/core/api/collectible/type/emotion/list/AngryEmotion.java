package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AngryEmotion extends EmotionCollectible {

    public AngryEmotion() {
        super("Brabo", CollectibleRarity.EPIC, new ArrayList<>(), 1, 1726024742401L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "7d41407363bcb46837538a63fdf7055278d42dc4aac639ed5794533bbd770",
                "47bbf6d9f4c57556eef816c50eb75f9d158f53954957aabe6c2e14ffa6c90",
                "a750127f1c3c71f6a5f5e9917a825e9235e1959b258ff29b6ff9771cb44",
                "e95b20fb1fcfbef222062dd43eecbcb3871c528665f8ed675f42fc6e589a0b7",
                "275c46184f9a85351d6ba618f8d1655cb5b71d6fc6ed3ccc462d916d376a8db",
                "fa151ceb66b3412775e9d44879046a398dbdb7dfcb0af571b7a03e72d9fbf1",
                "d82738fc82bedaf3029612f1ec92fe0cf848e541c8e30dcf41efc04bea30ba"
        ));

        for (int i = 0; i < 5; i++)
            frames.addAll(Arrays.asList(
                    "fa151ceb66b3412775e9d44879046a398dbdb7dfcb0af571b7a03e72d9fbf1",
                    "7f8db8cf241f2565c5bd495a0695b7cac9370c8bfd732d6d874e62fb12f3da",
                    "fa151ceb66b3412775e9d44879046a398dbdb7dfcb0af571b7a03e72d9fbf1",
                    "d82738fc82bedaf3029612f1ec92fe0cf848e541c8e30dcf41efc04bea30ba"
            ));

        setFrames(frames);
    }
}
