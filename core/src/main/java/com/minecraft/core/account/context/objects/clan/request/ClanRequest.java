package com.minecraft.core.account.context.objects.clan.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClanRequest {

    private final UUID id, author, receiver;
    private final long sentAt = System.currentTimeMillis();

    public String getIdentifier() {
        // clan-request:clanId:authorId:receiverId
        return id.toString() + ":" + author.toString() + ":" + receiver.toString();
    }
}
