package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountNickChangeMessage extends RedisMessage {

    private final UUID accountId;
    private final String nickname;

    public AccountNickChangeMessage(Account account, String nickname) {
        super(Constant.REDIS_ACCOUNT_NICK_CHANGE_CHANNEL);

        this.accountId = account.getId();
        this.nickname = nickname;
    }

    public Account getAccount() {
        // Primeiro tentar buscar no controller (cache local)
        Account account = Core.getAccountController().of(accountId);
        
        // Se não encontrar, tentar buscar no AccountData (PostgreSQL)
        if (account == null) {
            account = Core.getAccountData().of(accountId, false);
            
            // Se encontrou, salvar no controller para futuras buscas
            if (account != null) {
                Core.getAccountController().save(account);
            }
        }
        
        return account;
    }
}
