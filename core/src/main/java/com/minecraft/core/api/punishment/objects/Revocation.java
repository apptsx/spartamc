package com.minecraft.core.api.punishment.objects;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Revocation {

    @Builder.Default
    private final UUID author = Constant.DEFAULT_ID;

    @Builder.Default
    private final String reason = "Não informado";

    @Builder.Default
    private final long data = -1L;

    public boolean isValid() {
        return data > -1L;
    }

    public String getAuthorName() {
        if (author == null || author.equals(Constant.DEFAULT_ID))
            return "CONSOLE";
        com.minecraft.core.account.Account account = Core.getAccountData().of(author);
        return account != null ? account.getName() : "Desconhecido";
    }
}
