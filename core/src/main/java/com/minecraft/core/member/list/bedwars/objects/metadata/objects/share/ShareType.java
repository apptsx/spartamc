package com.minecraft.core.member.list.bedwars.objects.metadata.objects.share;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum ShareType {

    DEFAULT("Padrão", ChatColor.DARK_GRAY, asList(
            "§7Qualquer jogador pode compartilhar",
            "§7os favoritos com você.")),
    MEDIUM("Médio", ChatColor.YELLOW, asList(
            "§7Seus amigos, membros do clan e",
            "§7party podem compartilhar os",
            "§7favoritos com você.")),
    HARD("Difícil", ChatColor.GOLD, asList(
            "§7Seus amigos podem compartilhar",
            "§7os favoritos com você."
    )),
    MAX("Máximo", ChatColor.RED, asList(
            "§7Ninguém pode compartilhar os",
            "§7favoritos com você."
    ));

    private final String name;
    private final ChatColor color;

    private final List<String> lore;

    public ShareType next() {
        return this != MAX ? values()[ordinal() + 1] : DEFAULT;
    }
}
