package com.minecraft.core.account.context.objects.friend;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.friend.toggle.FriendToggle;
import com.minecraft.core.account.context.objects.friend.type.FriendType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Friend {

    private final UUID id;

    private FriendType type;
    private FriendToggle toggle;

    private final long startedAt;

    public Friend(Account account) {
        this.id = account.getId();

        this.type = FriendType.DEFAULT;
        this.toggle = new FriendToggle();

        this.startedAt = System.currentTimeMillis();
    }

    public boolean isValid() {
        return Core.getAccountData().of(id) != null;
    }

    public String getName() {
        Account account = Core.getAccountData().of(id);

        return isValid() ? account.getNickname() : "...";
    }
}
