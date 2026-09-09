package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

@Getter
@Setter
public class SellerCollectible extends BedCollectible {

    private String value, signature;

    public SellerCollectible(CollectibleRarity rarity, String name, Item icon, long createdAt) {
        super(BedCollectibleType.SELLER_SKIN, rarity, name, icon, createdAt);
    }

    public SellerCollectible(CollectibleRarity rarity, String name, long createdAt) {
        this(rarity, name, null, createdAt);
    }

    public boolean isNotEqual(Class<? extends SellerCollectible> sellerClass) {
        return !sellerClass.isAssignableFrom(getClass());
    }

    @Override
    public Item getIcon() {
        return super.getIcon() == null ? Item.of(Material.SKULL_ITEM, 3).skullByBase64(value) : super.getIcon();
    }

    public boolean isValid() {
        return value != null && signature != null && !getClass().getSimpleName().equalsIgnoreCase("Default");
    }
}
