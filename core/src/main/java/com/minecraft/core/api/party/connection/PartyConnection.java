package com.minecraft.core.api.party.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PartyConnection {

    private final String partyIdentifier;
    private final UUID userId;
}
