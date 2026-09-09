package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.account.context.objects.friend.Friend;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountFriendMessage extends RedisMessage {

    private final FriendMenuType type;

    private final UUID sender;
    private final Friend friend;

    public AccountFriendMessage(FriendMenuType type, UUID sender, Friend friend) {
        super(Constant.REDIS_ACCOUNT_FRIEND_MENU_CHANNEL);

        this.type = type;

        this.sender = sender;
        this.friend = friend;
    }

    public enum FriendMenuType {
        INFO, LIST, REQUEST
    }
}
