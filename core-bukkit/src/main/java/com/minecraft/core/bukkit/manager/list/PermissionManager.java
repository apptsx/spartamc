package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    public static void loadPermissions(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        PermissionAttachment attachment = player.addAttachment(BukkitCore.getInstance());

        // Iniciando lista com permissões do rank atual
        List<String> permissions = new ArrayList<>();
        if (account.getRank().getType() != null) {
            permissions.addAll(account.getRank().getType().getPermissions());
        }

        // Adicionando permissões da lista de ranks
        account.getAvailableRanks().forEach(rank -> {
            if (rank.getType() != null) {
                permissions.addAll(rank.getType().getPermissions());
            }
        });

        // Adicionando permissões da conta
        account.getPermissions().forEach(entry -> permissions.add(entry.getKey()));

        permissions.forEach(permission -> attachment.setPermission(permission, true));
    }

    public static void unloadPermissions(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            PermissionAttachment attachment = permission.getAttachment();

            if (attachment == null) return;

            Map<String, Boolean> flags = attachment.getPermissions();

            flags.forEach((key, value) -> attachment.setPermission(key, false));
        }
    }
}