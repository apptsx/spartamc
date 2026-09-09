package com.minecraft.core.api.clan.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum ClanRole {

    MEMBER("Membro", "☆", ChatColor.GRAY),
    RECRUITER("Recrutador", "✸", ChatColor.GREEN),
    LEADER("Líder", "✵", ChatColor.RED);

    private final String name;
    private final String symbol;

    private final ChatColor color;
}
