package com.minecraft.core.api.collectible.type.emotion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheekyEmotion extends EmotionCollectible {

    public CheekyEmotion() {
        super("Dando Língua", CollectibleRarity.EPIC, new ArrayList<>(), 1, 1726025168885L);

        List<String> frames = new ArrayList<>();

        for (int i = 0; i < 4; i++)
            frames.add("319ec094258842725e41f985346a7f824af6fc6cf13fbe949c9c465a30bc99");

        frames.addAll(Arrays.asList(
                "b7d533e65f2cae97afe334c81ecc97e2fa5b3e5d3ecf8b91bc39a5adb2e79a",
                "35a46f8334e49d273384eb72b2ac15e24a640d7648e4b28c348efce93dc97ab",
                "1d977753c3db742865ccf14c5c3f482eaf5721414750e6d3be96e1ae7c8291c4",
                "c4188e6d90f7769dae3a7277e2490d01b8017d74a725fd3aebbc82a911aa4e",
                "1d977753c3db742865ccf14c5c3f482eaf5721414750e6d3be96e1ae7c8291c4",
                "447dcf9dd283ad6d83942b6607a7ce45bee9cdfeefb849da29d661d03e7938",
                "de355559f4cd56118b4bc8b4697b625e1845b635790c07bf4924c8c7673a2e4",
                "207eef91a453a5151487c9d6b9d4c434db7f8a02a4caf18ef6f3358677f6",
                "c4188e6d90f7769dae3a7277e2490d01b8017d74a725fd3aebbc82a911aa4e",
                "1d977753c3db742865ccf14c5c3f482eaf5721414750e6d3be96e1ae7c8291c4",
                "447dcf9dd283ad6d83942b6607a7ce45bee9cdfeefb849da29d661d03e7938",
                "de355559f4cd56118b4bc8b4697b625e1845b635790c07bf4924c8c7673a2e4",
                "207eef91a453a5151487c9d6b9d4c434db7f8a02a4caf18ef6f3358677f6"
        ));

        for (int i = 0; i < 10; i++)
            frames.add("c4188e6d90f7769dae3a7277e2490d01b8017d74a725fd3aebbc82a911aa4e");

        setFrames(frames);
    }
}
