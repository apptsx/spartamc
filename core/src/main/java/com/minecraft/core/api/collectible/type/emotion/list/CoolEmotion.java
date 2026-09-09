package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoolEmotion extends EmotionCollectible {

    public CoolEmotion() {
        super("Legal", CollectibleRarity.EPIC, new ArrayList<>(), 2, 1726025120630L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "a21e6dbfd74a1859ddbae3380fc1ab71f2389745945fc92329b164635bd14f",
                "3733db9a94bfe15cdbb7ca5832c85cfada98ad2c839934766bdc41f977b5c163"
        ));

        for (int i = 0; i < 4; i++)
            frames.add("766b3eef3c726ecb816c43839189eeb8e36382e3e5fe41128372785185a322");

        setFrames(frames);
    }
}
