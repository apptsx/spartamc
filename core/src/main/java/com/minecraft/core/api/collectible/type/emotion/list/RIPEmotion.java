package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;

public class RIPEmotion extends EmotionCollectible {

    public RIPEmotion() {
        super("RIP", CollectibleRarity.RARE, new ArrayList<>(), 2, 1726025168885L);

        addFrames("30e78285d5aee0b28787ad88a5d58fb05ccf22918daa516ead85a6bf4fe068",
                "63611b5724e091854e79926fd11e486bfd0f99042721c3b34177f818639c19d",
                "83e0621b45d3a326d236293cd8ea49ae74d52e56fc8d1d133e7fc8bcf2a5988",
                "6e16a7ae186c3cfeac364eac0e83d3528741c3dd9ef8277080e03deabc714",
                "20ec3a80ed35bd9beb7d20cb75f1ecd5b8ab0d576f1db699f7def13131fbc5",
                "b03badcc9fb966c87e0dc1332d735b2b587c2602d35fecb44ba6ed94ceb4",
                "439c3df7a628af8d751ecca197642cdc1a07c30e3289b2d3261f7a65cf395b");
    }
}
