package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.clan.request.ClanRequest;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.api.clan.color.ClanColor;
import com.minecraft.core.api.clan.member.ClanMember;
import com.minecraft.core.api.clan.role.ClanRole;
import com.minecraft.core.backend.data.list.api.ClanData;
import com.minecraft.core.backend.database.redis.message.types.account.AccountClanChangeMessage;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.bungee.manager.ClanTagManager;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClanCommand implements CommandInheritor {

    private final ClanData data = Core.getClanData();

    protected final Pattern NAME_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{3,7}$"),
            TAG_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{3,7}$");

    private final String ALREADY_IN_A_CLAN = "§cVocê já faz parte de um clan!",
            NOT_PART_OF_CLAN = "§cVocê não faz parte de um clan!";

    @Completer(name = "clan")
    public List<String> clanCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String command = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(command))
                    list.add(subCommand);
            }
        } else
            list.addAll(subCommands);

        return list;
    }

    @Command(name = "clan", aliases = {"clans"})
    public void clan(BungeeCommandContext context) {
        Account account = context.getAccount();

        String label = "/" + context.getLabel();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase()))
            account.send("§cComo usar " + label + ":",
                    "§e* " + label + " criar (nome) (tag)",
                    "§e* " + label + " deletar",
                    "§e* " + label + " sair",
                    "§e* " + label + " ver (nome/tag)",
                    "§e* " + label + " convidar [jogador]",
                    "§e* " + label + " aceitar [jogador]",
                    "§e* " + label + " recusar [jogador]",
                    "§e* " + label + " transferir [jogador]",
                    "§e* " + label + " cortag (cor)"
            );
    }

    @Command(name = "clan.criar", aliases = {"clan.create", "clan.criar"})
    public void clanCreate(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length <= 1) {
            account.send("§cUso: /" + context.getCommandLabel() + " (nome) (tag).");
            return;
        }

        String name = args[0], tag = args[1];

        if (!NAME_VALIDATOR.matcher(name).matches() || name.length() < 3 || name.length() > 7) {
            account.send("§cO nome do seu clan não é válido!",
                    "§cEntre 3 e 7 caracteres!");
            return;
        }

        if (data.getByName(name) != null) {
            account.send("§cOps! Um clan com o nome " + name + " já existe.");
            return;
        }

        int minTagLength = 3;
        
        if (!TAG_VALIDATOR.matcher(tag).matches() || tag.length() < minTagLength || tag.length() > 7) {
            account.send("§cA tag do seu clan não é válida!",
                    "§cEntre " + minTagLength + " e 7 caracteres!");
            return;
        }

        if (data.getByTag(tag) != null) {
            account.send("§cOps! Um clan com a tag " + tag + " já existe.");
            return;
        }

        Clan clan = new Clan(account, UUID.randomUUID(), name, tag);
        
        // Definir cor padrão (Cinza) na criação do clan
        clan.setTagColor(ClanTagManager.getDefaultColor().getCode());

        data.save(clan);

        account.setClan(clan.getId());
        account.send("§aO seu clan foi criado com sucesso.");

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.deletar", aliases = {"clan.delete", "clan.disband"})
    public void clanDelete(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.LEADER)) {
            account.send("§cVocê não pode deletar o seu clan atual.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (confirmar).");
            return;
        }

        if (args[0].equalsIgnoreCase("confirmar"))
            account.getClan().disband();
    }

    @Command(name = "clan.convidar", aliases = {"clan.adicionar", "clan.invitar"})
    public void clanInvite(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cVocê não pode convidar jogadores para o clan.");
            return;
        }

        Clan clan = account.getClan();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (target.hasClan()) {
            account.send("§cO jogador " + target.getNickname() + " já faz parte de um clan!");
            return;
        }

        if (data.hasRequest(clan, target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " já foi convidado para o clan!");
            return;
        }

        account.send("§aVocê convidou " + target.getNickname() + " para o seu clan!");

        // Adicionando requisição no Redis
        data.addRequest(new ClanRequest(clan.getId(), account.getId(), target.getId()));

        // Envia notificação apenas se o jogador estiver online
        if (target.isOnline()) {
            if (!target.getToggle().isAllowClanInvite()) {
                return;
            }

            TextComponent message = new TextComponent("§6" + account.getNickname() + "§e convidou você para o clan §b" + clan.getName() + "§e!");

            TextComponent acceptButton = new TextComponent("§a§lACEITAR");

            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan aceitar " + account.getNickname()));
            acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para aceitar o convite.")));

            TextComponent declineButton = new TextComponent("§c§lRECUSAR");

            declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan recusar " + account.getNickname()));
            declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para recusar o convite.")));

            message.addExtra("\n        ");
            message.addExtra(acceptButton);
            message.addExtra("        ");
            message.addExtra(declineButton);

            target.send(message);
        }
    }

    @Command(name = "clan.aceitar", aliases = {"clan.accept", "clan.join"})
    public void clanAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        Clan clan = data.of(request.getId());

        data.removeRequest(request);

        if (clan == null) {
            account.send("§cNão foi possível encontrar o clan.");
            return;
        }

        clan.addMember(account);

        account.send("§aVocê aceitou o convite de " + target.getNickname() + " para entrar no clan §7" + clan.getName() + "§a.");
        target.send("§a" + account.getNickname() + " aceitou o seu convite para entrar no clan.");

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.sair", aliases = {"clan.quit", "clan.left"})
    public void clanLeave(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account == null) {
            context.getSender().send("§cNão foi possível carregar sua conta.");
            return;
        }

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (account.isClanLeader()) {
            account.send("§cVocê não pode sair do seu clan!");
            return;
        }

        Clan clan = account.getClan();

        if (clan == null) {
            account.send("§cErro: Clan não encontrado.");
            return;
        }

        if (!clan.isMember(account.getId())) {
            account.send("§cVocê não é membro deste clan.");
            // Limpar referência ao clan caso esteja inconsistente
            account.setClan(Constant.DEFAULT_ID);
            return;
        }

        clan.removeMember(account);

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.transferir", aliases = {"clan.promote", "clan.lead"})
    public void clanTransfer(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.isClanLeader()) {
            account.send("§cVocê não pode transferir o clan para outros jogadores.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        Clan clan = account.getClan();

        if (!clan.isMember(target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " não é membro do seu clan.");
            return;
        }

        clan.setLeader(target.getId());

        clan.send("§7O clan foi transferido para " + target.getNickname() + ".");
    }

    @Command(name = "clan.recusar", aliases = {"clan.deny", "clan.reject"})
    public void clanDecline(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        data.removeRequest(request);

        account.send("§cVocê recusou o convite de " + target.getNickname() + " para entrar no clan.");
        target.send("§c" + account.getNickname() + " recusou o convite para entrar no clan.");
    }

    @Command(name = "clan.cortag", aliases = {"clan.tag"})
    public void clanTag(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = account.getClan();
        ClanMember member = clan.getMember(account.getId());
        
        if (member == null) {
            account.send("§cErro: Você não é membro deste clã.");
            return;
        }
        
        ClanRole role = member.getRole();
        if (role != ClanRole.LEADER && role != ClanRole.RECRUITER) {
            account.send("§cApenas o líder e o recrutador podem alterar a cor da tag do clã.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            ClanTagManager.sendColorMenu(account, clan);
            return;
        }

        String colorInput = args[0];
        String colorCode = ClanColor.parse(colorInput);

        if (colorCode == null) {
            account.send("§cCor inválida!");
            ClanTagManager.sendColorMenu(account, clan);
            return;
        }

        ClanColor clanColor = ClanColor.getByCode(colorCode);
        
        if (clanColor == null) {
            account.send("§cErro ao processar a cor.");
            return;
        }

        if (!ClanTagManager.canUseColorForClan(account, clan, clanColor)) {
            account.send("§cVocê não tem permissão para usar esta cor!");
            ClanTagManager.sendColorMenu(account, clan);
            return;
        }

        ClanTagManager.applyColor(clan, clanColor);

        if (clanColor.hasGradient()) {
            account.send("§aA tag do clã foi alterada para o gradiente " + colorCode + clanColor.getName() + "§a.");
        } else {
            account.send("§aA cor da tag do clã foi alterada para " + colorCode + clanColor.getName() + "§a.");
        }

        clan.send("§7A cor da tag do clã foi alterada para " + colorCode + clanColor.getName() + "§7 por " + account.getNickname() + ".");

        clan.getOnlineMembers().forEach(memberAccount -> new AccountClanChangeMessage(memberAccount).send());
    }

    @Command(name = "clan.addcor")
    public void clanAddCor(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.getRank().getType().isHigherOrEqual(com.minecraft.core.account.context.objects.rank.type.RankType.ADMIN)) {
            account.send("§cVocê não tem permissão para usar este comando.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length < 2) {
            account.send("§cUso: /clan addcor <clan> <cor>");
            account.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
            return;
        }

        String clanName = args[0];
        String colorInput = args[1];

        Clan clan = Core.getClanData().getByName(clanName);

        if (clan == null) {
            account.send("§cClan não encontrado: §e" + clanName);
            return;
        }

        String colorCode = ClanColor.parse(colorInput);

        if (colorCode == null) {
            account.send("§cCor inválida: §e" + colorInput);
            account.send("§7Cores disponíveis: " + ClanColor.getFormattedList());
            return;
        }

        ClanColor clanColor = ClanColor.getByCode(colorCode);

        if (clanColor == null) {
            account.send("§cErro ao processar a cor.");
            return;
        }

        clan.setTagColor(colorCode);
        ClanTagManager.applyColor(clan, clanColor);

        account.send("§aCor §e" + clanColor.getName() + "§a adicionada ao clan §e" + clan.getName() + "§a.");
        clan.send("§aUma cor especial (§e" + clanColor.getName() + "§a) foi adicionada ao clan por um administrador.");

        clan.getOnlineMembers().forEach(memberAccount -> new AccountClanChangeMessage(memberAccount).send());
    }
}
