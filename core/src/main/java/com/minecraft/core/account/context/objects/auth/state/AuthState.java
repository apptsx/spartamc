package com.minecraft.core.account.context.objects.auth.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthState {

    PENDENT("Pendente"),
    OK("Concluído");

    private final String name;
}
