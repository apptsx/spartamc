package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WinkEmotion extends EmotionCollectible {

    public WinkEmotion() {
        super("Piscadela", CollectibleRarity.MYTHICAL, new ArrayList<>(), 2, 1726025059639L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "ef66c7485ccb853a6538122e45c8a4821fbe097f96a6060feb981f7a2bba890",
                "a5d43eb0ec5f6de1d469b69680978a6dd7117772ee0d82ffdf08749e84df7ed"
        ));

        for (int i = 0; i < 2; i++)
            frames.add("473e72cc371de25f3305665769dd7e9ff1161695252e799612580deeedd3");

        for (int i = 0; i < 4; i++)
            frames.add("f4ea2d6f939fefeff5d122e63dd26fa8a427df90b2928bc1fa89a8252a7e");

        setFrames(frames);
    }
}
