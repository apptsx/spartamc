package com.minecraft.core.api.clan.member;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.clan.role.ClanRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanMember {

    private final UUID id;
    private String name;

    private ClanRole role;

    private final long joinedAt;

    public ClanMember(Account account, ClanRole role) {
        this.id = account.getId();
        this.name = account.getName();

        this.role = role;
        this.joinedAt = System.currentTimeMillis();
    }

    public boolean hasRole(ClanRole role) {
        return this.role.equals(role);
    }
}
