package com.minecraft.core.account.context.objects.auth;

import com.minecraft.core.account.context.objects.auth.state.AuthState;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AuthMetadata {

    private String password = "", lastPassword = "";

    private AuthState state = AuthState.PENDENT;

    private final List<String> recentPasswords = new ArrayList<>();

    private long timestamp = System.currentTimeMillis();
}
