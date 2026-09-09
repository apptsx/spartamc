package com.minecraft.core.bukkit.event.type.account;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@Getter
@RequiredArgsConstructor
public class AccountRefreshEvent extends EventHandler {

    private final Account account;
    private final Field field;

    private final Object old, value;
}
