package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmazedEmotion extends EmotionCollectible {

    public AmazedEmotion() {
        super("Espantado", CollectibleRarity.MYTHICAL, new ArrayList<>(), 1, 1726024994311L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "6b7f24bb6a4585de8f42303161bded5c8398ce375631be149460d6835aec44e",
                "33c760f660d447846ab6b3d5a914c4b01f10672b63d4311d468b6dc28ba0e3",
                "382d15e94182206025973ff1928f4456bf7abaff737942d54b1c5699892c",
                "9d641bd33180c53dcc77e3d4c665935e63011d87ae9796a2ae7bd334cd64"
        ));

        for (int i = 0; i < 8; i++)
            frames.add("4c3b089e446f065dd9059519c85c45aebb53891be3c3a7ed5b5eb61a96747");

        setFrames(frames);
    }
}
