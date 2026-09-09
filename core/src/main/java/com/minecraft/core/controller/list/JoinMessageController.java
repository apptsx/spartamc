package com.minecraft.core.controller.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.joinmessage.JoinMessageRarity;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.joinmessage.JoinMessage;
import com.minecraft.core.api.joinmessage.JoinMessageType;
import com.minecraft.core.controller.Controller;

import java.util.List;

public class JoinMessageController extends Controller<String, JoinMessage> {

    @Override
    public void save(JoinMessage message) {
        getCache().put(message.getId().toLowerCase(), message);
    }

    public void handle() {
        Core.getLogger().info("Carregando mensagens de entrada...");
        
        // Carregar mensagens do enum
        for (JoinMessageType type : JoinMessageType.values()) {
            save(type.toJoinMessage());
        }
        
        Core.getLogger().info(size() + " mensagens de entrada foram carregadas.");
    }

    public JoinMessage of(String id) {
        JoinMessage message = super.of(id.toLowerCase());
        if (message == null) {
            // Tentar buscar no enum
            JoinMessageType type = JoinMessageType.byId(id);
            if (type != null) {
                message = type.toJoinMessage();
                save(message);
            }
        }
        return message;
    }

    public List<JoinMessage> list(JoinMessageRarity rarity) {
        return filter(message -> message.getRarity().equals(rarity));
    }

    public List<JoinMessage> list(RankType rank) {
        return filter(message -> message.hasAccess(rank));
    }
}
