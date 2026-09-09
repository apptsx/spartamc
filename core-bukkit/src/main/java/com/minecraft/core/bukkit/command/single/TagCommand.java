package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.flag.ServerFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TagCommand implements CommandInheritor {

    @Command(name = "tag")
    public void tag(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY) + " para mudar a sua tag novamente.");
            return;
        }

        if (args.length == 0) {
            TagManager.sendTags(account);
            return;
        }

        Tag tag = Tag.of(args[0]);

        if (tag == null || !account.hasTag(tag)) {
            account.send("§cA tag informada não foi encontrada ou você não a possui.");
            return;
        }

        if (account.isUsingTag(tag)) {
            account.send("§cA tag " + tag.getColoredName() + "§c já foi selecionada.");
            return;
        }

        account.setTag(tag);

        if (!account.getServerType().hasFlag(ServerFlag.NAME_TAG_CHANGE))
            TagManager.updateTag(account, tag);

        account.send("§aA tag " + tag.getColoredName() + "§a foi selecionada.");

        if (!account.isStaffer())
            account.setCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(5));
    }

    @Completer(name = "tag")
    public List<String> tagsCompleter(BukkitCommandContext context) {
        List<String> tagList = new ArrayList<>();

        String[] args = context.getArgs();

        Account account = context.getAccount();

        List<Tag> availableTags = TagManager.getAvailableTags(account);

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

            for (Tag tag : availableTags) {
                String name = tag.getName().toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

                if (name.startsWith(filter))
                    tagList.add(tag.getName());
            }
        } else {
            for (Tag tag : availableTags) {
                tagList.add(tag.getName());
            }
        }

        return tagList;
    }
}