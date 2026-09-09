package com.minecraft.core.api.joinmessage;

import com.minecraft.core.account.context.objects.joinmessage.JoinMessageRarity;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import lombok.Getter;

@Getter
public enum JoinMessageType {
    
    // Mensagens padrão
    DEFAULT("default", "Padrão", "§6entrou no lobby!", JoinMessageRarity.COMMON, RankType.MEMBER),
    CONNECTED("connected", "Conectado", "§6se conectou!", JoinMessageRarity.COMMON, RankType.MEMBER),
    ARRIVED("arrived", "Chegou", "§6chegou chegando!", JoinMessageRarity.COMMON, RankType.MEMBER),
    
    // Mensagens VIP
    VIP_SURFING("vip_surfing", "Surfando", "§6chegou surfando no lobby!", JoinMessageRarity.COMMON, RankType.VIP),
    
// Mensagens MAX
    MAX_ARRIVED("max_arrived", "Chegou", "§6chegou chegando!", JoinMessageRarity.COMMON, RankType.MAX),
    MAX_LANDED("max_landed", "Aterrissou", "§6aterrissou no lobby!", JoinMessageRarity.COMMON, RankType.MAX),
    MAX_FELL("max_fell", "Caiu", "§6caiu no lobby!", JoinMessageRarity.COMMON, RankType.MAX),
    
    // Mensagens ADMIN
    ADMIN_ARRIVED("admin_arrived", "Chegou", "§6entrou no lobby!", JoinMessageRarity.EPIC, RankType.ADMIN),
    
// Mensagem CHEFE (exclusiva para CHEFE, não visível para ranks acima)
    CHEFE_DEVELOPING("chefe_developing", "Desenvolvendo", "§6chegou desenvolvendo!", JoinMessageRarity.EPIC, RankType.CHEFE, true);
    
    private final String id;
    private final String name;
    private final String message;
    private final JoinMessageRarity rarity;
    private final RankType requiredRank;
    private final boolean exactRank;
    
    JoinMessageType(String id, String name, String message, JoinMessageRarity rarity, RankType requiredRank) {
        this(id, name, message, rarity, requiredRank, false);
    }
    
    JoinMessageType(String id, String name, String message, JoinMessageRarity rarity, RankType requiredRank, boolean exactRank) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.rarity = rarity;
        this.requiredRank = requiredRank;
        this.exactRank = exactRank;
    }
    
    public JoinMessage toJoinMessage() {
        return new JoinMessage(id, name, message, rarity, requiredRank, exactRank);
    }
    
    public static JoinMessageType byId(String id) {
        for (JoinMessageType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}