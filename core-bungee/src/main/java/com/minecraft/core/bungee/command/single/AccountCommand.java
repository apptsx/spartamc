package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.info.RankInfo;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.clan.color.ClanColor;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.geography.GeographyService;
import com.minecraft.core.api.geography.GeographyBuilder;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AccountCommand implements CommandInheritor {

    private final List<String> subCommands = Arrays.asList("rank", "golds", "permission", "punishments", "medal", "reload", "clantag", "tag", "title");

    @Completer(name = "account", subCommands = {"acc"}, rank = RankType.MODPLUS)
    public List<String> accountCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "account", aliases = {"acc"}, onlyPlayer = false)
    public void account(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        Account staffer = context.getAccount();

        if (args.length == 0) {
            if (!sender.isPlayer()) {
                sender.send("§cUso: /acc <jogador>");
                return;
            }
            
            if (staffer == null) {
                sender.send("§cNão foi possível carregar sua conta.");
                return;
            }
            
            sendInfoMessage(sender, staffer);
            return;
        }

        if (sender.isPlayer() && staffer != null && !staffer.hasRank(RankType.MODPLUS)) {
            sender.send("§cVocê não pode ver a conta de outros jogadores.");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (args.length == 1) {
            sendInfoMessage(sender, target);
            return;
        }

        String label = "/" + context.getLabel();

        String search = args[1].toLowerCase();

        if (subCommands.stream().noneMatch(subCommand -> subCommand.equalsIgnoreCase(search))) {
            sender.send("§cComo usar " + label + ":");

            subCommands.forEach(subCommand -> sender.send("§c* " + label + " [jogador] " + subCommand));
            return;
        }

        switch (search) {
            case "rank": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " rank:",
                            "§c " + label + " [jogador] rank set [tipo] [tempo]",
                            "§c " + label + " [jogador] rank info");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] rank set (tipo) [tempo].");
                            return;
                        }

                        RankType rankType = RankType.of(args[3]);

                        if (rankType == null) {
                            sender.send("§cO rank solicitado não foi encontrado.");
                            return;
                        }

                        if (rankType == RankType.CHEFE) {
                            String targetName = args[0].toLowerCase();
                            if (!targetName.equals(com.minecraft.core.Constant.CHEFE_OWNER_NAME.toLowerCase())) {
                                sender.send("§cO rank CHEFE só pode ser definido para §b" + com.minecraft.core.Constant.CHEFE_OWNER_NAME + "§c!");
                                return;
                            }
                        }

                        if (sender.isPlayer()) {
                            assert staffer != null;
                            if (staffer.getRankType().equals(rankType) || !staffer.hasRank(rankType)) {
                                sender.send("§cVocê não pode atribuir ranks similares ou iguais ao seu.");
                                return;
                            }
                        }

                        if (sender.isPlayer()) {
                            assert staffer != null;
                            if (!staffer.hasRank(target.getRank().getType())) {
                                sender.send("§cVocê não pode atribuir ranks para jogadores superiores.");
                                return;
                            }
                        }

                        if (target.hasAvailableRank(rankType) || target.hasOnlyRank(rankType)) {
                            sender.send("§cO jogador " + target.getName() + " já possui o rank " + rankType.getName() + ".");
                            return;
                        }

                        // Verificar se o rank é exclusivo (apenas 1 por vez)
                        if (rankType == RankType.ADMIN || rankType == RankType.CHEFE || rankType == RankType.MOD || rankType == RankType.MODPLUS) {
                            // Remover o rank de qualquer outro jogador que já tenha
                            Core.getAccountController().list().forEach(acc -> {
                                if (acc.hasRank(rankType) && !acc.getId().equals(target.getId())) {
                                    acc.setRank(Rank.builder()
                                            .type(RankType.MEMBER)
                                            .assignment(Assignment.CONSOLE)
                                            .author(sender.getId())
                                            .build());
                                    acc.saveContext(acc.getContext());
                                }
                            });
                        }

                        long expiresAt = -1L;

                        if (args.length >= 5)
                            expiresAt = TimeUtil.getTime(args[4]);

                        Rank rank = Rank.builder()
                                .type(rankType)
                                .assignment(sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE)
                                .author(sender.getId())
                                .expiresAt(expiresAt)
                                .build();

                        target.setRank(rank);

                        sender.send("§aVocê adicionou o rank " + rankType.getName() + " na conta de " + target.getNickname() + ".");

                        log(sender, sender.getName() + " adicionou o rank " + rankType.getName() + " na conta de " + target.getNickname());
                        
                        com.minecraft.core.bungee.service.discord.DiscordService.getInstance().sendRankChangeEmbed(
                                target.getNickname(), 
                                rankType.getName(), 
                                rankType.getColor().toString(), 
                                sender.getName(), 
                                "ADD"
                        );
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] rank remove (tipo).");
                            return;
                        }

                        RankType rankType = RankType.of(args[3]);

                        if (rankType == null) {
                            sender.send("§cO rank solicitado não foi encontrado.");
                            return;
                        }

                        if (rankType.equals(RankType.MEMBER)) {
                            sender.send("§cVocê não pode remover o rank " + RankType.MEMBER.getName() + " da conta de " + target.getNickname() + ".");
                            return;
                        }

                        if (!(target.hasOnlyRank(rankType) || target.hasAvailableRank(rankType))) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui o rank " + rankType.getName() + ".");
                            return;
                        }

                        target.removeRank(rankType);

                        sender.send("§eO rank " + rankType.getName() + " de " + target.getNickname() + " foi removido com sucesso.");
                        
                        com.minecraft.core.bungee.service.discord.DiscordService.getInstance().sendRankChangeEmbed(
                                target.getNickname(), 
                                rankType.getName(), 
                                rankType.getColor().toString(), 
                                sender.getName(), 
                                "REMOVE"
                        );

                        break;
                    }

                    case "info": {
                        RankInfo info = target.getContext().getRankInfo();

                        Rank rank = info.getRank();
                        List<Rank> rankList = info.getAvailableRanks();

                        /* Criar mensagem de lista dos ranks */
                        StringBuilder builder = new StringBuilder();

                        builder.append("§aLista de Ranks (§f").append(rankList.size()).append("§a): ");

                        int index = 1;
                        boolean end = false;
                        for (Rank extraRank : rankList) {
                            if (index >= rankList.size()) end = true;

                            builder.append(extraRank.getColoredName()).append("§f").append(end ? "." : ", ");

                            index++;
                        }

                        if (rankList.isEmpty())
                            builder.append("§fVázio.");

                        sender.send("§aRank: " + rank.getColoredName(),
                                "§aAtribuído por: §f" + rank.getAuthorName(),
                                " §7Data de atribuição: " + DateUtil.getDate(rank.getAssignedAt()),
                                " §6§o" + rank.getAssignment().getName(),
                                builder.toString());
                        break;
                    }
                }
                break;
            }

            case "permission": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " permission:",
                            "§c* " + label + " [jogador] permission set (chave) [tempo]",
                            "§c* " + label + " [jogador] permission remove (chave)",
                            "§c* " + label + " [jogador] permission list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length <= 4) {
                            sender.send("§cUso: " + label + " [jogador] permission set (chave) [tempo].");
                            return;
                        }

                        String key = args[3];

                        if (target.hasPermission(key)) {
                            sender.send("§cO jogador " + target.getNickname() + " já tem essa permissão.");
                            return;
                        }

                        long expiresAt = TimeUtil.getTime(args[4]);

                        Permission permission = new Permission(key, sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE, sender.getId(), expiresAt);

                        target.setPermission(permission);

                        sender.send("§aVocê adicionou a permissão '" + key + "' na conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] permission remove (chave).");
                            return;
                        }

                        String key = args[3];

                        if (!target.hasPermission(key)) {
                            sender.send("§cO jogador " + target.getNickname() + " não tem essa permissão.");
                            return;
                        }

                        target.removePermission(key);

                        sender.send("§cVocê removeu a permissão '" + key + "' da conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "list": {
                        List<Permission> list = target.getPermissions();

                        if (list.isEmpty()) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui permissões.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aPermissões: ");


                        int index = 1;
                        boolean end = false;
                        for (Permission permission : list) {
                            if (index >= list.size()) end = true;

                            builder.append("§f").append(permission.getKey()).append(end ? "." : ", ");

                            index++;
                        }

                        sender.send(builder.toString());
                        break;
                    }
                }

                break;
            }

            case "punishments": {
                PunishmentCategory category = null;

                if (args.length >= 3) {
                    category = PunishmentCategory.of(args[2]);
                    if (category == null) {
                        sender.send("§cA categoria '" + args[2] + "' não foi encontrada.");
                        sender.send("§cCategorias disponíveis: ban, mute, skin, report");
                        return;
                    }
                }

                List<Punishment> punishments = category != null
                        ? target.getPunishments(category)
                        : target.getHistory().getPunishments(p -> true);

                if (punishments.isEmpty()) {
                    sender.send("§cO jogador " + target.getNickname() + " não possui histórico de punições" +
                            (category != null ? " da categoria " + category.getName() : "") + ".");
                    return;
                }

                sender.send("");
                sender.send("§6§l┃ Histórico de Punições ┃ " + target.getNickname());
                sender.send("§8Quantidade: " + punishments.size() + " puniç" + (punishments.size() == 1 ? "ão" : "ões"));

                for (Punishment punishment : punishments) {
                    List<String> lines = new ArrayList<>(Arrays.asList(
                            "",
                            " §8▪ §fID: §7" + punishment.getId(),
                            "   §fCategoria: " + (punishment.getCategory() == PunishmentCategory.BAN ? "§c" : "§e") + punishment.getCategory().getName(),
                            "   §fMotivo: §7" + punishment.getCause(),
                            "   §7§o" + punishment.getReason().getName(),
                            "   §fAutor: §7" + punishment.getAuthorName(),
                            "   §fData: §7" + DateUtil.getDate(punishment.getCreatedAt()),
                            "   §fSituação: " + (punishment.isValid() ? "§aAtivo" : "§cRevogado")
                    ));

                    if (punishment.isValid()) {
                        if (punishment.isTemporary())
                            lines.add("   §fExpira em: §7" + TimeUtil.formatTime(punishment.getExpiresAt(), TimeUtil.TimeFormat.SHORT));
                        else
                            lines.add("   §fExpira em: §7Nunca (permanente)");
                    }

                    if (punishment.getRevocation() != null && punishment.getRevocation().isValid()) {
                        lines.add("   §fRevogado por: §7" + punishment.getRevocation().getAuthorName());
                        lines.add("   §fRevogado em: §7" + DateUtil.getDate(punishment.getRevocation().getData()));
                        lines.add("   §fMotivo: §7" + punishment.getRevocation().getReason());
                    }

                    lines.add("   §fServidor: §7" + punishment.getServer().getName());

                    if (punishment.getArcade() != null && !punishment.getArcade().equals(ArcadeCategory.NONE))
                        lines.add("   §fModo: §7" + punishment.getArcade().getName());

                    sender.send(lines);
                }

                sender.send("");
                break;
            }

            case "golds": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " golds:",
                            "§c* " + label + " [jogador] golds add (valor)",
                            "§c* " + label + " [jogador] golds remove (valor)");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] golds add (valor).");
                            return;
                        }

                        if (!Util.isNumber(args[3])) {
                            sender.send("§cSomente números são válidos.");
                            return;
                        }

                        int value = Integer.parseInt(args[3]);

                        if (value <= 0) {
                            sender.send("§cO valor de ouros não pode ser menor ou igual a zero.");
                            return;
                        }

                        target.addGolds(value);

                        sender.send("§aVocê adicionou " + Util.formatNumber(value) + " ouros na conta de " + target.getNickname() + ".");
                        break;
                    }
                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] golds remove (valor).");
                            return;
                        }

                        if (!Util.isNumber(args[3])) {
                            sender.send("§cSomente números são válidos.");
                            return;
                        }

                        int value = Integer.parseInt(args[3]);

                        if (value <= 0) {
                            sender.send("§cO valor de golds não pode ser menor ou igual a zero.");
                            return;
                        }

                        target.removeGolds(value);

                        sender.send("§eVocê removeu " + Util.formatNumber(target.getGolds() - value) + " golds da conta de " + target.getNickname() + ".");
                        break;
                    }
                }

                break;
            }

            case "medal": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " medal:",
                            "§c* " + label + " [jogador] medal add (nome)",
                            "§c* " + label + " [jogador] medal remove (nome)",
                            "§c* " + label + " [jogador] medal set (nome)",
                            "§c* " + label + " [jogador] medal list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] medal add (nome).");
                            return;
                        }

                        Medal medal = Medal.of(args[3]);

                        if (medal == null) {
                            sender.send("§cA medalha '" + args[3] + "' não foi encontrada.");
                            return;
                        }

                        if (medal.equals(Medal.NONE)) {
                            sender.send("§cVocê não pode adicionar a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        if (target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " já possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.addMedal(medal);

                        sender.send("§aVocê adicionou a medalha " + medal.getColoredName() + " na conta de " + target.getNickname() + ".");
                        log(sender, sender.getName() + " adicionou a medalha " + medal.getName() + " na conta de " + target.getNickname());
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] medal remove (nome).");
                            return;
                        }

                        Medal medal = Medal.of(args[3]);

                        if (medal == null) {
                            sender.send("§cA medalha '" + args[3] + "' não foi encontrada.");
                            return;
                        }

                        if (medal.equals(Medal.NONE)) {
                            sender.send("§cVocê não pode remover a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        if (!target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.removeMedal(medal);

                        // Se a medalha removida estava sendo usada, definir como NONE
                        if (target.isUsingMedal(medal)) {
                            target.setMedal(Medal.NONE);
                        }

                        sender.send("§eVocê removeu a medalha " + medal.getColoredName() + " da conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "set": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] medal set (nome).");
                            return;
                        }

                        Medal medal = Medal.of(args[3]);

                        if (medal == null) {
                            sender.send("§cA medalha '" + args[3] + "' não foi encontrada.");
                            return;
                        }

                        if (!target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.setMedal(medal);

                        sender.send("§aVocê definiu a medalha " + medal.getColoredName() + " como ativa na conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "list": {
                        List<Medal> medals = target.getMedals();
                        Medal activeMedal = target.getMedal();

                        if (medals.isEmpty()) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui medalhas.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aMedalhas: ");

                        int index = 1;
                        boolean end = false;
                        for (Medal medal : medals) {
                            if (index >= medals.size()) end = true;

                            String prefix = medal.equals(activeMedal) ? "§e[ATIVA] " : "";
                            builder.append(prefix).append(medal.getColoredName()).append("§f").append(end ? "." : ", ");

                            index++;
                        }

                        sender.send(builder.toString());
                        if (activeMedal != null && !activeMedal.equals(Medal.NONE)) {
                            sender.send("§aMedalha ativa: " + activeMedal.getColoredName());
                        }
                        break;
                    }
                }

                break;
            }

            case "clantag": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " clantag:",
                            "§c* " + label + " [jogador] clantag add (cor)",
                            "§c* " + label + " [jogador] clantag remove (cor)",
                            "§c* " + label + " [jogador] clantag list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] clantag add (cor).");
                            sender.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
                            return;
                        }

                        String colorInput = args[3];
                        String colorCode = ClanColor.parse(colorInput);

                        if (colorCode == null) {
                            sender.send("§cCor inválida: §e" + colorInput);
                            sender.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
                            return;
                        }

                        if (target.hasClanTagColorPermission(colorCode)) {
                            sender.send("§cO jogador §e" + target.getNickname() + " §cjá tem permissão para usar esta cor.");
                            return;
                        }

                        target.addClanTagColor(colorCode);

                        ClanColor clanColor = ClanColor.getByCode(colorCode);
                        sender.send("§aVocê adicionou a cor §e" + (clanColor != null ? clanColor.getName() : colorCode) + "§a para §e" + target.getNickname() + "§a usar em clans.");
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] clantag remove (cor).");
                            sender.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
                            return;
                        }

                        String colorInput = args[3];
                        String colorCode = ClanColor.parse(colorInput);

                        if (colorCode == null) {
                            sender.send("§cCor inválida: §e" + colorInput);
                            sender.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
                            return;
                        }

                        if (!target.hasClanTagColorPermission(colorCode)) {
                            sender.send("§cO jogador §e" + target.getNickname() + " §cnão tem permissão para usar esta cor.");
                            return;
                        }

                        target.removeClanTagColor(colorCode);

                        ClanColor clanColor = ClanColor.getByCode(colorCode);
                        sender.send("§cVocê removeu a cor §e" + (clanColor != null ? clanColor.getName() : colorCode) + "§c de §e" + target.getNickname() + "§c.");
                        break;
                    }

                    case "list": {
                        List<String> colors = target.getAllowedClanTagColors();

                        if (colors.isEmpty()) {
                            sender.send("§cO jogador §e" + target.getNickname() + " §cnão tem permissões de cor de clantag.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aCores de clantag permitidas: ");
                        for (int i = 0; i < colors.size(); i++) {
                            String code = colors.get(i);
                            ClanColor clanColor = ClanColor.getByCode(code);
                            String colorName = clanColor != null ? clanColor.getName() : code;
                            boolean end = (i == colors.size() - 1);
                            builder.append(code).append(colorName).append(end ? "." : "§7, ");
                        }

                        sender.send(builder.toString());
                        break;
                    }

                    default: {
                        sender.send("§cUso: " + label + " [jogador] clantag add/remove/list (cor)");
                        break;
                    }
                }

                break;
            }

            case "tag": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " tag:",
                            "§c* " + label + " [jogador] tag set (nome)",
                            "§c* " + label + " [jogador] tag add (nome)",
                            "§c* " + label + " [jogador] tag remove",
                            "§c* " + label + " [jogador] tag list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] tag set (nome).");
                            return;
                        }

                        com.minecraft.core.account.context.objects.tag.Tag tag = com.minecraft.core.account.context.objects.tag.Tag.of(args[3]);

                        if (tag == null) {
                            sender.send("§cA tag '" + args[3] + "' não foi encontrada.");
                            return;
                        }

                        if (!tag.isRole(com.minecraft.core.account.context.objects.tag.role.TagRole.SPECIAL)) {
                            sender.send("§cApenas tags especiais podem ser definidas (Carnaval, Halloween, Ferias, Natal, 2026, Champion).");
                            return;
                        }

                        target.setTag(tag);

                        sender.send("§aVocê definiu a tag " + tag.getColoredName() + " para " + target.getNickname() + ".");
                        break;
                    }

                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] tag add (nome).");
                            return;
                        }

                        com.minecraft.core.account.context.objects.tag.Tag tag = com.minecraft.core.account.context.objects.tag.Tag.of(args[3]);

                        if (tag == null) {
                            sender.send("§cA tag '" + args[3] + "' não foi encontrada.");
                            return;
                        }

                        if (!tag.isRole(com.minecraft.core.account.context.objects.tag.role.TagRole.SPECIAL)) {
                            sender.send("§cApenas tags especiais podem ser adicionadas (Carnaval, Halloween, Ferias, Natal, 2026, Champion).");
                            return;
                        }

                        target.setTag(tag);

                        sender.send("§aVocê adicionou a tag " + tag.getColoredName() + " para " + target.getNickname() + ".");
                        break;
                    }

                    case "remove": {
                        target.setTag(null);

                        sender.send("§cA tag de " + target.getNickname() + " foi removida.");
                        break;
                    }

                    case "list": {
                        com.minecraft.core.account.context.objects.tag.Tag activeTag = target.getTag();

                        sender.send("§aTag ativa: §f" + (activeTag != null ? activeTag.getColoredName() : "Nenhuma"));
                        sender.send("§7Tags disponíveis: TAG_CARNAVAL, TAG_HALLOWEEN, TAG_FERIAS, TAG_NATAL, TAG_2026, TAG_CHAMPION");
                        break;
                    }
                }
                break;
            }

            case "title": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " title:",
                            "§c* " + label + " [jogador] title add (nome)",
                            "§c* " + label + " [jogador] title remove (nome)",
                            "§c* " + label + " [jogador] title set (nome)",
                            "§c* " + label + " [jogador] title list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] title add (nome).");
                            return;
                        }

                        String titleName = args[3];
                        String collectibleId = "TITLE:" + titleName;

                        com.minecraft.core.api.collectible.Collectible collectible = com.minecraft.core.Core.getCollectibleController().of(collectibleId);

                        if (collectible == null) {
                            sender.send("§cO título '" + titleName + "' não foi encontrado.");
                            return;
                        }

                        if (!target.hasCollectible(collectibleId)) {
                            target.setCollectible(collectibleId);
                        }

                        sender.send("§aVocê adicionou o título '" + collectible.getName() + "' para " + target.getNickname() + ".");
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] title remove (nome).");
                            return;
                        }

                        String titleName = args[3];
                        String collectibleId = "TITLE:" + titleName;

                        sender.send("§cA remoção de títulos não é suportada via comando. Use o menu do jogador.");
                        break;
                    }

                    case "set": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] title set (nome).");
                            return;
                        }

                        String titleName = args[3];
                        String collectibleId = "TITLE:" + titleName;

                        com.minecraft.core.api.collectible.Collectible collectible = com.minecraft.core.Core.getCollectibleController().of(collectibleId);

                        if (collectible == null) {
                            sender.send("§cO título '" + titleName + "' não foi encontrado.");
                            return;
                        }

                        if (!target.hasCollectible(collectibleId)) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui o título '" + titleName + "'.");
                            return;
                        }

                        target.activateCollectible(collectible);

                        sender.send("§aVocê definiu o título '" + collectible.getName() + "' como ativo para " + target.getNickname() + ".");
                        break;
                    }

                    case "list": {
                        List<com.minecraft.core.api.collectible.Collectible> titles = target.getCollectibles(CollectibleCategory.TITLE);

                        if (titles.isEmpty()) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui títulos.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aTítulos: ");

                        int index = 1;
                        boolean end = false;
                        for (com.minecraft.core.api.collectible.Collectible c : titles) {
                            if (index >= titles.size()) end = true;

                            builder.append("§f").append(c.getName()).append(end ? "." : ", ");

                            index++;
                        }

                        sender.send(builder.toString());
                        break;
                    }
                }
                break;
            }

            case "reload": {
                Core.getAccountController().remove(target.getId());
                Core.getAccountData().removeCache(target);
                
                Account reloadedAccount = Core.getAccountData().of(target.getId(), true);
                
                if (reloadedAccount == null) {
                    sender.send("§cNão foi possível recarregar a conta de " + target.getName() + ".");
                    return;
                }
                
                sender.send("§aConta de " + target.getName() + " recarregada do banco de dados!");
                sender.send("§7Rank atual: " + reloadedAccount.getRank().getColoredName());
                break;
            }
        }
    }

    protected void sendInfoMessage(CommandSender sender, Account account) {
        List<String> message = new ArrayList<>(Arrays.asList(
                "§aUsuário: §f" + account.getName(),
                "§aTipo: §f" + (account.isPremium() ? "Original" : "Pirata"),
                "§aPrimeira entrada: §f" + DateUtil.getDate(account.getContext().getCreatedAt()),
                "§aÚltima entrada: §f" + DateUtil.getDate(account.getContext().getLastLogin())
        ));

        for (Rank rank : account.getAvailableRanks()) {
            message.addAll(Arrays.asList(
                    "§aRank: " + rank.getColoredName(),
                    " §7Autor: " + rank.getAuthorName(),
                    " §7Atribuído em: " + DateUtil.getDate(rank.getAssignedAt())));
        }

        message.addAll(Arrays.asList(
                "§aRank: " + account.getRank().getColoredName(),
                " §7Autor: " + account.getRank().getAuthorName(),
                " §7Atribuído em: " + DateUtil.getDate(account.getRank().getAssignedAt())
        ));

        message.add("§aEndereço IP: §f" + account.getIpAddress());

        GeographyBuilder geography = GeographyService.getGeography(account.getIpAddress());

        if (geography.isValid())
            message.addAll(Arrays.asList(
                    "§aLocalização:",
                    " §7País: " + geography.getCountry(),
                    " §7Estado: " + geography.getRegion(),
                    " §7Cidade: " + geography.getCity(),
                    " §7ASN: " + geography.getAsn()
            ));

        sender.send(message);
    }
}
