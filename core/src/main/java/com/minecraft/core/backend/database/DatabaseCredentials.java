package com.minecraft.core.backend.database;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DatabaseCredentials {

    private final String host, user, password, database;
    private final int port;
}
