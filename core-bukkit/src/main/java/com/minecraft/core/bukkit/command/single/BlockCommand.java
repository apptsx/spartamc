package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.block.Block;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.DateUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

public class BlockCommand implements CommandInheritor {

    @Command(name = "block", aliases = {"bloquear"})
    public void block(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        String label = "/" + context.getLabel();

        if (args.length == 0) {
            account.send("§cComo usar:" + label + ":",
                    "§c* " + label + " [jogador]",
                    "§c* " + label + " (lista)");
            return;
        }

        if (args[0].equalsIgnoreCase("lista")) {
            List<Block> list = account.getBlocks();

            if (list.isEmpty())
                account.send("§cVocê não bloqueou nenhum jogador!");
            else {
                TextComponent message = new TextComponent("Jogadores bloqueados (§a" + list.size() + "§f): ");

                int index = 1;
                boolean end = false;

                for (Block block : list) {
                    if (index >= list.size()) end = true;

                    Account blocked = context.getAccount(block.getId());

                    if (blocked != null) {
                        TextComponent content = new TextComponent("§f" + blocked.getNickname());

                        content.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                                "§7Data: " + DateUtil.getSimpleDate(block.getTimestamp())
                        )));

                        message.addExtra(content);
                        message.addExtra("§f" + (end ? "." : ", "));

                        index++;
                    }
                }

                account.send(message);
            }

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

        if (account.hasBlock(target)) {
            account.removeBlock(target);

            account.send("§eO jogador " + target.getNickname() + " foi desbloqueado.");
        } else {
            account.setBlock(target);

            account.send("§aO jogador " + target.getNickname() + " foi bloqueado.");
        }

    }
}
