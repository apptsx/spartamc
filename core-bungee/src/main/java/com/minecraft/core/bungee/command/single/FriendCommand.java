package com.minecraft.core.bungee.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.friend.Friend;
import com.minecraft.core.backend.database.redis.message.types.account.AccountFriendMessage;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.type.ServerType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class FriendCommand implements CommandInheritor {

    private final String NOT_FRIEND_MESSAGE = "§cVocê não possui amizade com esse jogador.";

    @Completer(name = "amigo", subCommands = {"friend"})
    public List<String> friendCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands(),
                playerList = context.getServerPlayerList();

        String[] args = context.getArgs();

        if (args.length == 1) {
            String command = args[0].toLowerCase();

            if (command.isEmpty())
                list.addAll(subCommands);
            else {
                for (String subCommand : subCommands) {
                    if (subCommand.toLowerCase().startsWith(command))
                        list.add(subCommand);
                }
            }

        } else if (args.length == 2) {
            String playerName = args[1].toLowerCase();

            if (playerName.isEmpty())
                list.addAll(playerList);
            else {
                for (String name : playerList) {
                    if (name.toLowerCase().startsWith(playerName))
                        list.add(name);
                }
            }
        }

        return list;
    }

    @Command(name = "amigo", aliases = {"amigos", "friends"})
    public void friend(BungeeCommandContext context) {
        String label = "/" + context.getLabel();

        if (context.getArgs().length == 0)
            context.getSender().send("§cComandos do " + label + ":",
                    "§c" + label + " lista - Visualizar lista de amizades.",
                    "§c" + label + " add [jogador] - Enviar um pedido de amizade.",
                    "§c" + label + " remove [jogador] - Encerrar amizade com um jogador.",
                    "§c" + label + " pedidos - Visualizar lista de pedidos.",
                    "§c" + label + " recusar [jogador] - Recusar um pedido de amizade.",
                    "§c" + label + " aceitar [jogador] - Aceitar um pedido de amizade.");
    }

    @Command(name = "amigo.lista", aliases = {"amigos", "friend.lista"})
    public void friendList(BungeeCommandContext context) {
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.LIST, context.getSender().getId(), null).send();
    }

    @Command(name = "amigo.pedidos", aliases = {"friend.pedidos"})
    public void friendRequest(BungeeCommandContext context) {
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.REQUEST, context.getSender().getId(), null).send();
    }

    @Command(name = "amigo.ver", aliases = {"friend.ver"})
    public void friendSee(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        // Usa AccountData diretamente para ver info de amigo offline
        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        Friend friend = account.getFriend(target.getId());

        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.INFO, account.getId(), friend).send();
    }

    @Command(name = "amigo.add", aliases = {"friend.add", "amigo.adicionar", "friend.adicionar", "amigos.add", "amigos.adicionar", "friends.add", "friends.adicionar"})
    public void friendAdd(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        // Usa AccountData diretamente para buscar offline
        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        // Não verifica mais ServerType.AUTH para permitir adicionar offline

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (account.isFriend(target)) {
            account.send("§cEsse jogador já é seu amigo.");
            return;
        }

        // Permite adicionar mesmo se o jogador estiver offline
        if (target.isOnline() && !target.getToggle().isAllowFriendRequests()) {
            account.send("§cO jogador " + target.getColoredName() + " §cnão está recebendo pedidos de amizade!");
            return;
        }

        if (target.hasFriendRequest(account)) {
            account.send("§cVocê já enviou uma solicitação de amizade para " + target.getColoredName() + "§c.");
            return;
        }

        account.send("§aVocê enviou uma solicitação de amizade para " + target.getColoredName() + "§a.");
        
        target.addFriendRequest(account);

        if (target.isOnline()) {
            TextComponent message = new TextComponent(account.getTag().getColor() + "§o" + account.getName() + " §6enviou uma solicitação de amizade para você!\n");
            
            TextComponent acceptButton = new TextComponent("§a§lACEITAR");
            acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§aClique para aceitar")));
            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo aceitar " + account.getName()));
            
            TextComponent denyButton = new TextComponent("§c§lNEGAR");
            denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§cClique para negar")));
            denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo negar " + account.getName()));
            
            message.addExtra(acceptButton);
            message.addExtra("       ");
            message.addExtra(denyButton);
            
            target.send(message);
        }
    }

    @Command(name = "amigo.remove", aliases = {"friend.remove"})
    public void friendRemove(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        // Usa AccountData diretamente para remover amigo offline
        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        account.removeFriend(target);
        target.removeFriend(account);

        account.send("§cVocê desfez a amizade com " + target.getColoredName() + "§c.");
        target.send("§eO jogador " + account.getColoredName() + "§e desfez a amizade com você.");
    }

    @Command(name = "amigo.aceitar", aliases = {"friend.aceitar", "amigo.aceita", "amigo.sim"})
    public void friendAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /amigo aceitar <jogador>.");
            return;
        }

        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send("§cJogador não encontrado.");
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cVocê não tem pedido de amizade pendente deste jogador.");
            return;
        }

        account.addFriend(target);
        target.addFriend(account);

        account.send("§aVocê aceitou a solicitação de §e" + target.getName() + "§a.");
        if (target.isOnline()) {
            target.send("§a§e" + account.getName() + "§a aceitou sua solicitação de amizade.");
        }

        account.removeFriendRequest(target.getId());
    }

    @Command(name = "amigo.negar", aliases = {"friend.negar", "amigo.recusar", "amigo.nao", "amigo.na"})
    public void friendDeny(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /amigo negar <jogador>.");
            return;
        }

        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send("§cJogador não encontrado.");
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cVocê não tem pedido de amizade pendente deste jogador.");
            return;
        }

        account.send("§cVocê拒绝了 a solicitação de §e" + target.getName() + "§c.");
        if (target.isOnline()) {
            target.send("§c§e" + account.getName() + "§c拒绝了 sua solicitação de amizade.");
        }

        account.removeFriendRequest(target.getId());
    }

    @Command(name = "amigo.negar", aliases = {"friend.negar", "amigo.recusar", "amigo.nao", "amigo.na"}, onlyPlayer = false)
    public void friendDenyOffline(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = com.minecraft.core.Core.getAccountData().of(args[0]);

        if (target == null) {
            account.send("§cJogador não encontrado. Verifique o nick e tente novamente.");
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cNão há solicitação de amizade pendente.");
            return;
        }

        account.send("§cVocê recusou a solicitação de amizade de " + target.getColoredName() + "§c.");
        if (target.isOnline()) {
            target.send("§c" + account.getColoredName() + "§c recusou a sua solicitação de amizade.");
        }

        account.removeFriendRequest(target.getId());
    }
}
