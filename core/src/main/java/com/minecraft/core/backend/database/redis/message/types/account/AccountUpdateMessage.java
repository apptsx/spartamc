package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import com.google.gson.JsonElement;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountUpdateMessage extends RedisMessage {

    private final UUID accountId;

    private final String field;
    private final JsonElement value;

    public AccountUpdateMessage(Account account, String field, JsonElement value) {
        super(Constant.REDIS_ACCOUNT_UPDATE_CHANNEL);

        this.accountId = account.getId();

        this.field = field;
        this.value = value;
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
