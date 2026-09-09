package com.minecraft.core.account.context.objects.fake;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fake {

    private String nick = "", lastNick = "";

    private long updatedAt = System.currentTimeMillis();

    public boolean isValid() {
        return nick != null && !nick.isEmpty();
    }
}
