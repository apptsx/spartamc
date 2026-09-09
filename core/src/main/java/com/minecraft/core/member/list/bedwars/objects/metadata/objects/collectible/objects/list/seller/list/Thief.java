package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;

public class Thief extends SellerCollectible {

    public Thief() {
        super(CollectibleRarity.RARE, "Ladrão", System.currentTimeMillis());

        setLore("§7Roubando... e vivendo!");

        setValue("ewogICJ0aW1lc3RhbXAiIDogMTYxMzI1NzI4OTgzNCwKICAicHJvZmlsZUlkIiA6ICIyNzc1MmQ2ZTUyYmM0MzVjYmNhOWQ5NzY1MjQ2YWNhNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZW1pbWVkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVkMDNhODRlMjI2MTg4ZDEyNGQ0NjIyOTQ2YWJiYTZkZjU2Mjk1ZmI2Njc1NDVmMmQ0YjRiZTQ0NTU2ZTc4MDkiCiAgICB9CiAgfQp9");
        setSignature("iJkefwD2cbvrfwytyh76D09X8tfybRWKw1udShD8p1DdXIaRtov0JI/Wii6GKWrIPNKh/rZibDR0c7vcG7b4LAi9dpAnqlqufYZM3NTgrtsLDB+1DYptgqe4RnA2rnL2JSOaJ2FXxKKZBi5g5/4LILertfoQDKThsMXqmX/Sf89Xt0BxO2iltem4ky+Chc2Fx+OtPN1wOBEICD34Sfabj+JkW0G3T2ufDbi0R24aTlmniI6fMcojyNbX4rNKm4/ckXQVXuHU9KcVdDwL+BK7cp57mI0rekMKGqUfWD0eJXWpwuiy4GTZX9H1zcnlee+u2Bg42gaWhNIkzt/cCdm4zAMGdwqOYCGaVfslGfQOGAGdK+jFjfZSqMBB6yFK/wDsmeQIhVXUIUms763H4AhNFCr4dt1HK5nX9+bZJMj6FyNThoNMAaEQ5GLReBa0juUQHfu/x443BEj5DXH19T0sCrmkN0mtKeETcgYXGZjZ/YaqiyZbyNaHpGe7h3tTGNI8OSJ9y0V2QqNFgc9zvQAjUKCO2wOBGVd6lEcvvHw5updcGvk8wh6jSr4cZPLo2E/1N8KWtVzjJ9rqApH8tkZKKdCILzzQxKH7E1YF7Hia/X/n9BEgRhfw8aV+lExINKEr/Hgg1fhq6KYzcYj60ioDnNEIVCjTichafvP+syaeJZg=");

        setRanks(RankType.MAX);
    }
}
