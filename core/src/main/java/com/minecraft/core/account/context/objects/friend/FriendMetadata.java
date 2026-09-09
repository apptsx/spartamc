package com.minecraft.core.account.context.objects.friend;

import com.minecraft.core.account.context.objects.friend.request.FriendRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class FriendMetadata {

    private final Map<UUID, Friend> friends = new HashMap<>();
    private final List<FriendRequest> requests = new ArrayList<>();

    public boolean isFull() {
        return friends.size() >= 12;
    }
}
