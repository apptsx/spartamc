package com.minecraft.core.bukkit.event.type.account;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountVanishLogEvent extends EventHandler {

    private final Account account;
    private final VanishState state;

    public enum VanishState {
        JOIN, LEAVE
    }

    public boolean isState(VanishState state) {
        return this.state.equals(state);
    }
}
