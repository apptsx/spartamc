package com.minecraft.core.account.context.objects.punishment;

import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class PunishmentHistory {

    private final List<Punishment> punishments = new ArrayList<>();

    private final boolean bypass;

    /* --------------------------
       Gerenciamento de punições
    ---------------------------- */
    public List<Punishment> getPunishments(Predicate<Punishment> filter) {
        return punishments.stream().filter(filter).collect(Collectors.toList());
    }

    public List<Punishment> getPunishments(PunishmentCategory category) {
        return getPunishments(punishment -> punishment.getCategory().equals(category));
    }

    public void addPunishment(Punishment punishment) {
        this.punishments.add(punishment);
    }

    public void removePunishment(Punishment punishment) {
        this.punishments.remove(punishment);
    }

    public void updatePunishment(Punishment punishment) {
        if (punishments.stream().noneMatch(search -> search.getId().equals(punishment.getId()))) return;

        punishments.removeIf(search -> search.getId().equalsIgnoreCase(punishment.getId()));
        punishments.add(punishment);
    }

}
