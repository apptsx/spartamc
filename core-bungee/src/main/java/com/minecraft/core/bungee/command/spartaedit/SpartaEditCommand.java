package com.minecraft.core.bungee.command.spartaedit;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SpartaEditCommand extends Command {

    public SpartaEditCommand() {
        super("spartaedit", "spartaedit.use", "se");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null || !account.hasRank(RankType.ADMIN)) {
            player.sendMessage("§cVocê não tem permissão para usar o SpartaEdit.");
            return;
        }

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "pos1":
                player.sendMessage("§6[SPARTAEDIT] §eUse a ferramenta ou clique com o botão direito para definir a posição 1.");
                break;
            case "pos2":
                player.sendMessage("§6[SPARTAEDIT] §eUse a ferramenta ou clique com o botão direito para definir a posição 2.");
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
                player.sendMessage("§6[SPARTAEDIT] §eFerramenta ativada. Clique esquerdo para pos1, direito para pos2.");
                break;
            default:
                sendHelp(player);
        }
    }

    private void sendHelp(ProxiedPlayer player) {
        player.sendMessage("§6===== SpartaEdit Help ====");
        player.sendMessage("§e/se pos1 §7- Define a posição 1");
        player.sendMessage("§e/se pos2 §7- Define a posição 2");
        player.sendMessage("§e/se set <material> §7- Define blocos na seleção");
        player.sendMessage("§e/se replace <from> <to> §7- Substitui blocos");
        player.sendMessage("§e/se wand §7- Ativa ferramenta de seleção");
        player.sendMessage("§e/se clear §7- Limpa seleção");
    }
}
