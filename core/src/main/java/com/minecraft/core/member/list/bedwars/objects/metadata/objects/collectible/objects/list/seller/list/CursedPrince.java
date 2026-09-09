package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;

public class CursedPrince extends SellerCollectible {

    public CursedPrince() {
        super(CollectibleRarity.EPIC, "Principe Amaldiçoado", System.currentTimeMillis());

        setLore("§7No seu nascimento...", "§7uma bruxa o amaldiçoou!");

        setValue("ewogICJ0aW1lc3RhbXAiIDogMTczMTAwODQzMjQyOSwKICAicHJvZmlsZUlkIiA6ICJmYjZkM2E5Zjk3MWY0ZTdlYmQ0MjE2Yjk0MjE5NDA3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJjaXhkZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mNjZiMzk3NWViYzc4OTBmZjYxOGFlODcxZThlYjEyNjFiNTgxY2Y0MDdkZmVkNWI4YTE4NzRlZTA4YzZhZDEzIgogICAgfQogIH0KfQ==");
        setSignature("UkItFZ8z0doWfVMm5PTJZoi8yDxCIN4iVc/wu1GCNoV8IdwuPnJNLgCVFq7czyyzGFE5w0Q5M3XiKRkiNNwsAjoga3geUy5poGqRJbEmLxuyOQnC0jn1uRQyQ9NKN0g8FJMl1Xellr3voqcKfOWEnDpATgK2lvhg0fh3a4RkNFFEmGromXmAMdnRp8k0OHc0y/O6NszEjtLnLiT8IKbWROEkCrYO2NHIgaY2hE7b+Eq2UlgHXbabSMEb8DPM49UGFeYXQnZwI1wVpNCI4N6Hrf0+uTgGmsogQQp0cXZMS6e98QPk3PbqOl5UCuejYfCvDVpnlyMwyGX09fuEW2TcdMgh3pYqNoW7n48RtlBLrAqQ61tev1zfH0qpGgJ73JhaqOi4AbuiJPLQ+sovFWYH254WUtnBUMYvb0fi4VjcIYvunRPXF/jlyCEOVCc4Cyg9yiT9KYosJwp2BNr1+MNlhcnbYmh8djb3lRDBZWCYoiIGFeZWxwTcy5H41D/9rWAxea7TKBh4FI3DubZ9pewXMw/RXZduSco4+TTorNYoxbKIxw8a2ANMIENS4sAwt6OTBGyR9qfmZhhM+jVpNmz9AiiQy/PrkJHjZoSTgAFeGMk1/0I9+qcfC+z9zUlIe6JffbMyX1euBdMyntVCgi/y3t9nK6W9K17p1B+obN51Al0=");

        setRanks(RankType.VIP);
    }
}
