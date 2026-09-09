package com.minecraft.core.bukkit.command.spartaedit;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import org.bukkit.entity.Player;

public class SpartaEditCommand implements CommandInheritor {

    @Command(name = "spartaedit", aliases = {"se"}, rank = RankType.ADMIN)
    public void onCommand(BukkitCommandContext context) {
        Player player = context.getPlayer();
        String[] args = context.getArgs();

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "pos1":
                player.sendMessage("§6[SPARTAEDIT] §eUse a ferramenta para definir a posição 1.");
                break;
            case "pos2":
                player.sendMessage("§6[SPARTAEDIT] §eUse a ferramenta para definir a posição 2.");
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage("§6[SPARTAEDIT] §cUso: /se set <material>");
                    return;
                }
                player.sendMessage("§6[SPARTAEDIT] §eDefinindo blocos para " + args[1] + "...");
                break;
            case "replace":
                if (args.length < 3) {
                    player.sendMessage("§6[SPARTAEDIT] §cUso: /se replace <from> <to>");
                    return;
                }
                player.sendMessage("§6[SPARTAEDIT] §eSubstituindo " + args[1] + " por " + args[2] + "...");
                break;
            case "wand":
                player.sendMessage("§6[SPARTAEDIT] §eFerramenta ativada.");
                break;
            case "clear":
                player.sendMessage("§6[SPARTAEDIT] §eSeleção limpa.");
                break;
            default:
                sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== SpartaEdit Help ====");
        player.sendMessage("§e/se pos1 §7- Define a posição 1");
        player.sendMessage("§e/se pos2 §7- Define a posição 2");
        player.sendMessage("§e/se set <material> §7- Define blocos na seleção");
        player.sendMessage("§e/se replace <from> <to> §7- Substitui blocos");
        player.sendMessage("§e/se wand §7- Ativa ferramenta de seleção");
        player.sendMessage("§e/se clear §7- Limpa seleção");
    }
}
