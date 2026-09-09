package com.minecraft.core.api.joinmessage;

import com.minecraft.core.account.context.objects.joinmessage.JoinMessageRarity;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JoinMessage {

    private final String id;
    private final String name;
    private final String message;
    private final JoinMessageRarity rarity;
    private final RankType requiredRank;
    private final boolean exactRank;
    private final long createdAt;
    private List<String> lore = new ArrayList<>();
    private Item icon;

    public JoinMessage(String id, String name, String message, JoinMessageRarity rarity, RankType requiredRank) {
        this(id, name, message, rarity, requiredRank, false);
    }

    public JoinMessage(String id, String name, String message, JoinMessageRarity rarity, RankType requiredRank, boolean exactRank) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.rarity = rarity;
        this.requiredRank = requiredRank;
        this.exactRank = exactRank;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean hasAccess(RankType rank) {
        if (exactRank) {
            return rank == requiredRank;
        }
        return rank.ordinal() >= requiredRank.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        JoinMessage other = (JoinMessage) obj;
        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }
}
