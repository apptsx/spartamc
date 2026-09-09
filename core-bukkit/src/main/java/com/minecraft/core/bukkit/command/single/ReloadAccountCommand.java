package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadAccountCommand implements CommandInheritor {

    @Command(name = "reloadaccount", aliases = {"reloadacc", "racc"}, rank = RankType.ADMIN)
    public void reloadAccount(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length == 0) {
            context.getAccount().send("§cUso: /reloadaccount <jogador|all>");
            context.getAccount().send("§7/reloadaccount all - Recarrega todos online");
            context.getAccount().send("§7/reloadaccount <jogador> - Recarrega um jogador");
            return;
        }

        String target = args[0];

        if (target.equalsIgnoreCase("all")) {
            int reloaded = 0;
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (reloadPlayerAccount(online)) {
                    reloaded++;
                }
            }
            
            context.getAccount().send("§a" + reloaded + " conta(s) recarregada(s) do banco de dados!");
            Core.getLogger().info("[ReloadAccount] " + context.getAccount().getName() + " recarregou " + reloaded + " contas.");
            return;
        }

        // Recarregar conta de um jogador específico
        Player targetPlayer = Bukkit.getPlayer(target);
        
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            context.getAccount().send("§cJogador não está online: §f" + target);
            return;
        }

        if (reloadPlayerAccount(targetPlayer)) {
            context.getAccount().send("§aConta de §f" + targetPlayer.getName() + " §arecarregada do banco de dados!");
            targetPlayer.sendMessage("§a§lSUA CONTA FOI ATUALIZADA!");
            targetPlayer.sendMessage("§7Seus dados foram recarregados do banco de dados.");
            
            Core.getLogger().info("[ReloadAccount] " + context.getAccount().getName() + " recarregou a conta de " + targetPlayer.getName());
        } else {
            context.getAccount().send("§cErro ao recarregar conta de §f" + targetPlayer.getName());
        }
    }

    private boolean reloadPlayerAccount(Player player) {
        try {
            // Remover do cache em memória
            Core.getAccountController().remove(player.getUniqueId());
            
            // Carregar do banco de dados
            Account freshAccount = Core.getAccountData().of(player.getUniqueId(), false);
            
            if (freshAccount != null) {
                // Salvar no cache
                Core.getAccountController().save(freshAccount);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Core.getLogger().severe("[ReloadAccount] Erro ao recarregar conta de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Completer(name = "reloadaccount")
    public List<String> reloadAccountCompleter(BukkitCommandContext context, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");
            
            // Adicionar jogadores online
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            
            return suggestions;
        }
        return new ArrayList<>();
    }
}
