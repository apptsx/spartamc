package com.minecraft.core.bungee.command.structure.annotation;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bungee.command.structure.BungeeCommandHandler;
import com.minecraft.core.command.annotation.Completer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BungeeCompleter implements Listener {

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        String[] split = event.getCursor().replaceAll("\\s+", " ").split(" ");

        if (split.length == 0)
            return;

        String[] args = new String[split.length - 1];

        System.arraycopy(split, 1, args, 0, split.length - 1);

        String label = split[0].substring(1);

        Map<String, Map.Entry<Method, Object>> completers = BungeeCommandHandler.getCompleters();

        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }

            String cmdLabel = buffer.toString();

            if (completers.containsKey(cmdLabel)) {
                Map.Entry<Method, Object> entry = completers.get(cmdLabel);

                Completer completer = entry.getKey().getAnnotation(Completer.class);

                if (completer == null) {
                    Core.getLogger().warning("O completer " + cmdLabel + " é nulo!");
                    return;
                }

                if (completer.rank() != RankType.MEMBER && !account.hasRank(completer.rank())) return;

                try {
                    event.getSuggestions().clear();

                    List<String> list = (List<String>) entry.getKey().invoke(entry.getValue(),
                            new BungeeCommandContext(player, args, label, cmdLabel.split("\\.").length - 1));

                    event.getSuggestions().addAll(list);
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível realizar o completer de " + label, e);
                }
            }
        }
    }
}