package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;

public class LoveEmotion extends EmotionCollectible {

    public LoveEmotion() {
        super("Apaixonado", CollectibleRarity.COMUM, new ArrayList<>(), 2, 1726022860247L);

        addFrames("a080c9bfc64aeb5aed2acaa885d6fcbbd5e5ddf468956d3f1b1e455774d48893",
                "a4d678bb120fbd3beacaf36bdb117766a63d7c2d96a6e85a8ef5a2b13e166",
                "42737e99e4c0596a3712e7711baecae8d1ddb774ac1cf531896862380753e16",
                "a4d678bb120fbd3beacaf36bdb117766a63d7c2d96a6e85a8ef5a2b13e166",
                "a080c9bfc64aeb5aed2acaa885d6fcbbd5e5ddf468956d3f1b1e455774d48893",
                "a4d678bb120fbd3beacaf36bdb117766a63d7c2d96a6e85a8ef5a2b13e166",
                "42737e99e4c0596a3712e7711baecae8d1ddb774ac1cf531896862380753e16",
                "a4d678bb120fbd3beacaf36bdb117766a63d7c2d96a6e85a8ef5a2b13e166",
                "a080c9bfc64aeb5aed2acaa885d6fcbbd5e5ddf468956d3f1b1e455774d48893");
    }
}
