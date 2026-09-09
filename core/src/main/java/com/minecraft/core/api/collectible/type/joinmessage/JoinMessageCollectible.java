package com.minecraft.core.api.collectible.type.joinmessage;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.joinmessage.JoinMessage;
import com.minecraft.core.api.joinmessage.JoinMessageType;
import org.bukkit.Material;

import java.util.List;

public class JoinMessageCollectible extends Collectible {

    private final JoinMessage joinMessage;

    public JoinMessageCollectible(JoinMessage joinMessage) {
        super(joinMessage.getName(), CollectibleCategory.JOIN_MESSAGE, 
              convertRarity(joinMessage.getRarity()), 
              List.of(joinMessage.getRequiredRank()), 
              joinMessage.getCreatedAt());
        
        this.joinMessage = joinMessage;
        
        setIcon(Item.of(Material.BOOK));
        setLore(joinMessage.getLore());
    }
    
    private static CollectibleRarity convertRarity(com.minecraft.core.account.context.objects.joinmessage.JoinMessageRarity rarity) {
        switch (rarity) {
            case COMMON:
                return CollectibleRarity.COMUM;
            case EPIC:
                return CollectibleRarity.EPIC;
            case LEGENDARY:
                return CollectibleRarity.MYTHICAL;
            default:
                return CollectibleRarity.COMUM;
        }
    }
    
    public JoinMessage getJoinMessage() {
        return joinMessage;
    }
    
    public static void handle() {
        // Registrar todas as join messages como collectibles
        for (JoinMessageType type : JoinMessageType.values()) {
            JoinMessage message = type.toJoinMessage();
            JoinMessageCollectible collectible = new JoinMessageCollectible(message);
            Core.getCollectibleController().save(collectible);
        }
    }
}

