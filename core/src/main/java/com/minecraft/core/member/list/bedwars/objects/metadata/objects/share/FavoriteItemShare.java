package com.minecraft.core.member.list.bedwars.objects.metadata.objects.share;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class FavoriteItemShare {

    private final static String SHARE_KEY = "bed-share:";

    private final UUID sender, receiver;

    private final List<BedItem> items;

    private final long timestamp = System.currentTimeMillis();

    public FavoriteItemShare(UUID sender, UUID receiver, List<BedItem> items) {
        this.sender = sender;
        this.receiver = receiver;

        this.items = items;

        Core.getRedis().save(SHARE_KEY + sender + ":" + receiver, this, 60);
    }

    public static List<FavoriteItemShare> list() {
        return Core.getRedis().loadAll(SHARE_KEY, FavoriteItemShare.class);
    }

    public static FavoriteItemShare of(UUID sender, UUID receiver) {
        return Core.getRedis().load(SHARE_KEY + sender + ":" + receiver, FavoriteItemShare.class);
    }

    public static void remove(FavoriteItemShare share) {
        Core.getRedis().delete(SHARE_KEY + share.getSender());
    }

    public String getSenderName() {
        Account account = Core.getAccountData().of(sender);

        return account != null ? account.getNickname() : "...";
    }
}
