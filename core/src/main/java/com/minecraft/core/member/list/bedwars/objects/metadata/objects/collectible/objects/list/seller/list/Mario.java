package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;

public class Mario extends SellerCollectible {

    public Mario() {
        super(CollectibleRarity.EPIC, "Mario", System.currentTimeMillis());

        setLore("§7Mamma mia!");

        setValue("ewogICJ0aW1lc3RhbXAiIDogMTcwMDk3NDg0NzMwNiwKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAwOWY2ZmNkNTBhZGVhYjMxN2ExY2EwOTEwZmJkOGM3YWYyNzhlNzU3NmYyODY0ZDM2ODg1Mjg1NDUwZTI4MCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        setSignature("D+PPgv+H3MRJhQNrMV1kROPmlv53zKHIc0zfTR+xYX/MR704uRSvbjARVjx+Uu8fDfA9kijqAD3x1SmwLclwcZbTqKx54WM001bNHxlGuy81Sa+dQx3OkfPzg29HkNZo4OZSJCnxaT0tVs6DYWicGKc/CrMxoBpOupVJZnGoU+ZQKjhVk/7Z/XwqnsNIf44UWIxkqPs1VrHEnoEXWitWKGcg+sfWjfqrjLRTYzSEYqRVDKO8eN99/WItuxjLBU+pHK1e4jg6rtXyBBV+OgdkMCG2eT/Zp8kRp2DvZGvJcAgPU9SofktO9PdJsA8rzZg7oyJffZR03d+0SNk636k0clVApQOaAd/2YHtPnbbmeoJp2F4Nsi0L19myUk9ISidWFSaUK4kL04Wx6a6ddRiW9MqCLVnGdyzEtHNWve63k1bvJ+18Tj5M0oMlb07C+oz4fn06p2GsU9B0xDjj1HgPFIo8Lrkdd9+LgpRg7mQh22zT4M2szE7GtcRYUAeexol0eN9ZgaxCtexJmVOfWeL2Fez9c1wHpiT+PItj3S4zqpuAguJdizhUpcK5X7/OnjKEBzUK6sm36BTe78FNszjiZKLsRqdKCn1xwctxobuph182u9VjDjjhWm+MBYyxViaVuJ5FHDnCyRNfilr9ydJxzFWZd/pkw8I0RtZzAOleGyI=");

        setRanks(RankType.VIP);
    }
}
