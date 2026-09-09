package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.library.SkinLibrary;
import com.minecraft.core.api.skin.objects.SkinType;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.controller.list.SkinController;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkinCacheData {

    private final RedisDatabase redis;

    private final String SKIN_CACHE_KEY = "skin-cache:";

    public Skin of(Account account) {
        if (!account.isPremium()) return SkinLibrary.random();

        Skin skin = redis.load(SKIN_CACHE_KEY + account.getId(), Skin.class);

        if (skin == null) {
            skin = SkinController.getSkin(account.getId(), account.getName(), SkinType.PROFILE);

            if (skin == null) {
                skin = Skin.unknown();

                Core.getLogger().severe("[Skin-Cache] Não foi possível encontrar a skin de " + account.getName());
                return skin;
            }

            redis.save(SKIN_CACHE_KEY + account.getId(), skin, 60 * 5);
        }

        return skin;
    }

    public void clear() {
        redis.removeAll(SKIN_CACHE_KEY);
    }

}
