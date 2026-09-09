package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;

public class TDFWEmotion extends EmotionCollectible {

    public TDFWEmotion() {
        super("TDFW", CollectibleRarity.MYTHICAL, new ArrayList<>(), 3, 1726024742401L);

        addFrames("72bb8ba79648718fe80687ed4df2b9e284e732583e05658e227efd7fdf80f4",
                "29b5b1f2c92a1283456f608b29ec3617191aba2bd31bd4b4b08e6cba6806227",
                "7959ef5fabb3f83fb19bba6ca67bb97758eec60235cf46e71d834b237337c4",
                "6313411e97963d104322218967a85a5d691330bad5f7192e3781d9565ebbdf",
                "fa3f7f2f6970d32db284261520c8c441fe4b3268ac0c99aeb4a5248656bd");
    }
}
