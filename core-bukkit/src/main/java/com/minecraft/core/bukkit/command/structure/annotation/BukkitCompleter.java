package com.minecraft.core.bukkit.command.structure.annotation;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Completer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class BukkitCompleter implements TabCompleter {

    private final Map<String, Map.Entry<Method, Object>> completers = new HashMap<>();

    public void addCompleter(String label, Method method, CommandInheritor commandInheritor) {
        completers.put(label, new AbstractMap.SimpleEntry<>(method, commandInheritor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Account account = Core.getAccountController().of(sender.getName());

        if (account == null) return new ArrayList<>();

        for (int i = args.length; i >= 0; i--) {
            StringBuilder builder = new StringBuilder();

            builder.append(label.toLowerCase());

            for (int y = 0; y < i; y++) {
                if (!args[y].isEmpty())
                    builder.append(".").append(args[y].toLowerCase());
            }

            String cmdLabel = builder.toString();

            if (completers.containsKey(cmdLabel)) {
                Map.Entry<Method, Object> entry = completers.get(cmdLabel);

                Completer completer = entry.getKey().getAnnotation(Completer.class);

                if (completer == null) {
                    Core.getLogger().warning("O completer " + cmdLabel + " é nulo!");
                    return new ArrayList<>();
                }

                if (completer.rank() != RankType.MEMBER && !account.hasRank(completer.rank())) return new ArrayList<>();

                try {
                    return (List<String>) entry.getKey().invoke(entry.getValue(),
                            new BukkitCommandContext(sender, args, label, cmdLabel.split("\\.").length - 1));
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o completer...", e);
                }
            }
        }

        return null;
    }
}