package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.enums.BedSkin;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;

public class Default extends SellerCollectible {

    public Default() {
        super(CollectibleRarity.COMUM, "Padrão", System.currentTimeMillis());

        setValue(BedSkin.RED.getValue());
        setSignature(BedSkin.RED.getSignature());
    }
}
