package com.minecraft.core.api.party.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PartyRequest {
    private final String partyIdentifier;
    private final UUID sender, receiver;
}
