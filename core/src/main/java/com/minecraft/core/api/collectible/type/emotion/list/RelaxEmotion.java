package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelaxEmotion extends EmotionCollectible {

    public RelaxEmotion() {
        super("Relaxado", CollectibleRarity.MYTHICAL, new ArrayList<>(), 3, 1726024994311L);

        List<String> frames = new ArrayList<>(Arrays.asList(
                "927ebbf5c2535fe6b5cef8b8a7e1e7067a39ed21ba547f83fce4472184d80c7",
                "fd2dc4db780294919517306964d65ea078b47b823fda5628be48b6ae61c",
                "e744e61b46a5f749ccaa2bf8132981b8986234359f9a2924f547ec0111e4375",
                "f673dd2ced78d89dac845d395443d6d39bcfe9c74c71a652029487b9281",
                "720f158ea771adf0862e7c1a19f01409cddd6edfd64385682ed7bc653eda3",
                "b6d6581ee0ec93ca9d5f4afbf6e28f5a9582a896ccccb9e7c17e6419e597e27",
                "e08876a49b1abbad149724be3eae35aa6305c529e384c118ba381a81e2df59e"
        ));

        for (int i = 0; i < 2; i++) {
            frames.addAll(Arrays.asList(
                    "927ebbf5c2535fe6b5cef8b8a7e1e7067a39ed21ba547f83fce4472184d80c7",
                    "b6d6581ee0ec93ca9d5f4afbf6e28f5a9582a896ccccb9e7c17e6419e597e27"
            ));
        }

        frames.addAll(Arrays.asList(
                "cc86703cc5839d413e393f173dde4fb71cfc965e1d254ae7d7bb38bf0a233d5",
                "762c3a6265418977a564fa9376fb5b1a87f9f8b8052c63a2d51817691e4223a"
        ));

        setFrames(frames);
    }
}
