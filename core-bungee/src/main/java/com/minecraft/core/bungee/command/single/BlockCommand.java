package com.minecraft.core.bungee.command.single;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.block.Block;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.DateUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

public class BlockCommand implements CommandInheritor {

    @Command(name = "block", aliases = {"bloquear"})
    public void block(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        String label = "/" + context.getLabel();

        if (args.length == 0) {
            account.send("§cComo usar " + label + ":",
                    "§c* " + label + " [jogador]",
                    "§c* " + label + " (lista)");
            return;
        }

        String request = args[0];

        if (request.equalsIgnoreCase("lista")) {
            List<Block> list = account.getBlocks();

            if (list.isEmpty()) {
                account.send("§cVocê ainda não bloqueou ninguém!");
                return;
            }

            TextComponent message = new TextComponent("§aContas bloqueadas (§f" + list.size() + "§a): ");

            int index = 1;
            boolean end = false;
            for (Block block : list) {
                if (index >= list.size()) end = true;

                TextComponent template = new TextComponent("§f" + block.getName() + (end ? "." : ", "));

                template.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                        "§7Data: §f" + DateUtil.getSimpleDate(block.getTimestamp())
                )));

                message.addExtra(template);
                index++;
            }

            account.send(message);
            return;
        }

        Account target = context.getAccount(request);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        boolean blocked = account.hasBlock(target);

        if (blocked)
            account.removeBlock(target);
        else
            account.setBlock(target);

        account.send((blocked ? "§cVocê desbloqueou" : "§aVocê bloqueou")
                + " o jogador " + target.getNickname() + ".");
    }
}
