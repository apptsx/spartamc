package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HappyEmotion extends EmotionCollectible {

    public HappyEmotion() {
        super("Alegre", CollectibleRarity.RARE, new ArrayList<>(), 1, 1726024161985L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "ef66c7485ccb853a6538122e45c8a4821fbe097f96a6060feb981f7a2bba890",
                "a5d43eb0ec5f6de1d469b69680978a6dd7117772ee0d82ffdf08749e84df7ed"
        ));

        for (int i = 0; i < 7; i++)
            frames.add("473e72cc371de25f3305665769dd7e9ff1161695252e799612580deeedd3");

        for (int i = 0; i < 15; i++)
            frames.add("01b9def55876c41c17c815f88115f02c95f89620fbed6a6cb2d38d46fe05");

        setFrames(frames);
    }
}
