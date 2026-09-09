package com.minecraft.core.backend.database.redis.message.types.route.arcade;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ArenaPartyWarpMessage extends RedisMessage {

    private final UUID receiverId;
    private final ArcadeRouteContext route;

    public ArenaPartyWarpMessage(Account receiver, ArcadeRouteContext route) {
        super(Constant.REDIS_ARENA_PARTY_WARP_CHANNEL);

        this.receiverId = receiver.getId();
        this.route = route;
    }

    public Account getReceiver() {
        // Primeiro tentar buscar no controller (cache local)
        Account account = Core.getAccountController().of(receiverId);
        
        // Se não encontrar, tentar buscar no AccountData (PostgreSQL)
        if (account == null) {
            account = Core.getAccountData().of(receiverId, false);
            
            // Se encontrou, salvar no controller para futuras buscas
            if (account != null) {
                Core.getAccountController().save(account);
            }
        }
        
        return account;
    }
}
