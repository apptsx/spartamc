package com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.user;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TopUser {

    private final UUID id;
    private int value;

    public Account getAccount() {
        return Core.getAccountData().of(id);
    }
}
