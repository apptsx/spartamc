package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.member.Member;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadMemberCommand implements CommandInheritor {

    @Command(name = "reloadmember", aliases = {"reloadmem", "rmem"}, rank = RankType.ADMIN)
    public void reloadMember(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getAccount().send("§cUso: /reloadmember <jogador|all> <tipo>");
            context.getAccount().send("§7Tipos: bedwars, skywars, duels, pvp, all");
            context.getAccount().send("§7Exemplo: /reloadmember Steve bedwars");
            context.getAccount().send("§7Exemplo: /reloadmember all all");
            return;
        }

        String target = args[0];
        String type = args[1].toLowerCase();

        List<String> types = new ArrayList<>();
        if (type.equals("all")) {
            types.addAll(Arrays.asList("bedwars", "skywars", "duels", "pvp"));
        } else if (Arrays.asList("bedwars", "skywars", "duels", "pvp").contains(type)) {
            types.add(type);
        } else {
            context.getAccount().send("§cTipo inválido! Use: bedwars, skywars, duels, pvp, all");
            return;
        }

        if (target.equalsIgnoreCase("all")) {
            int reloaded = 0;
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                for (String memberType : types) {
                    if (reloadPlayerMember(online, memberType)) {
                        reloaded++;
                    }
                }
            }
            
            context.getAccount().send("§a" + reloaded + " member(s) recarregado(s) do banco de dados!");
            Core.getLogger().info("[ReloadMember] " + context.getAccount().getName() + " recarregou " + reloaded + " members.");
            return;
        }

        // Recarregar member de um jogador específico
        Player targetPlayer = Bukkit.getPlayer(target);
        
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            context.getAccount().send("§cJogador não está online: §f" + target);
            return;
        }

        int reloaded = 0;
        for (String memberType : types) {
            if (reloadPlayerMember(targetPlayer, memberType)) {
                reloaded++;
            }
        }

        if (reloaded > 0) {
            context.getAccount().send("§a" + reloaded + " member(s) de §f" + targetPlayer.getName() + " §arecarregado(s)!");
            targetPlayer.sendMessage("§a§lSEUS DADOS FORAM ATUALIZADOS!");
            targetPlayer.sendMessage("§7Suas estatísticas foram recarregadas do banco de dados.");
            
            Core.getLogger().info("[ReloadMember] " + context.getAccount().getName() + " recarregou " + reloaded + " members de " + targetPlayer.getName());
        } else {
            context.getAccount().send("§cErro ao recarregar members de §f" + targetPlayer.getName());
        }
    }

    private boolean reloadPlayerMember(Player player, String type) {
        try {
            Class<? extends Member> memberClass = getMemberClass(type);
            if (memberClass == null) return false;

            // Remover do cache em memória
            Core.getMemberController().remove(player.getUniqueId(), memberClass);
            
            // Carregar do banco de dados
            Member freshMember = loadMemberFromDatabase(player.getUniqueId(), type);
            
            if (freshMember != null) {
                // Salvar no cache
                Core.getMemberController().save(freshMember);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Core.getLogger().severe("[ReloadMember] Erro ao recarregar " + type + " de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Member loadMemberFromDatabase(java.util.UUID uuid, String type) {
        switch (type.toLowerCase()) {
            case "bedwars":
                return Core.getBedWarsData().of(uuid, false);
            case "skywars":
                return Core.getSkyWarsData().of(uuid, false);
            case "duels":
                return Core.getDuelsData().of(uuid, false);
            case "pvp":
                return Core.getPvpData().of(uuid, false);
            default:
                return null;
        }
    }

    private Class<? extends Member> getMemberClass(String type) {
        try {
            switch (type.toLowerCase()) {
                case "bedwars":
                    return Class.forName("com.minecraft.core.member.list.bedwars.BedMember").asSubclass(Member.class);
                case "skywars":
                    return Class.forName("com.minecraft.core.member.list.skywars.SkyMember").asSubclass(Member.class);
                case "duels":
                    return Class.forName("com.minecraft.core.member.list.duels.DuelMember").asSubclass(Member.class);
                case "pvp":
                    return Class.forName("com.minecraft.core.member.list.pvp.PvPMember").asSubclass(Member.class);
                default:
                    return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Completer(name = "reloadmember")
    public List<String> reloadMemberCompleter(BukkitCommandContext context, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");
            
            // Adicionar jogadores online
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            
            return suggestions;
        } else if (args.length == 2) {
            return Arrays.asList("bedwars", "skywars", "duels", "pvp", "all").stream()
                    .filter(type -> type.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
