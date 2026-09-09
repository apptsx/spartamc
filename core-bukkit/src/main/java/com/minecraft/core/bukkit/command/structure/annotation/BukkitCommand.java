package com.minecraft.core.bukkit.command.structure.annotation;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.command.annotation.Command;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.minecraft.core.command.CommandInheritor.WITHOUT_PERMISSION;

@Getter
@Setter
public class BukkitCommand extends org.bukkit.command.Command {

    private final JavaPlugin holder;
    private final CommandExecutor executor;

    private BukkitCompleter completer;
    private RankType rank;

    public BukkitCommand(JavaPlugin holder, RankType rank, String label) {
        super(label);

        this.holder = holder;
        this.executor = holder;

        this.rank = rank;
    }

    @SneakyThrows
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> completions = null;
        try {
            if (completer != null) {
                completions = completer.onTabComplete(sender, this, alias, args);
            }
            if (completions == null && executor instanceof TabCompleter) {
                completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
            }
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

            for (String arg : args) {
                message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("' in plugin ")
                    .append(holder.getDescription().getFullName());

            throw new CommandException(message.toString(), ex);
        }

        if (completions == null) {
            return super.tabComplete(sender, alias, args);
        }
        return completions;
    }

    public synchronized boolean handleCommand(CommandSender sender, String label, String[] args) {
        try {
            for (int index = args.length; index >= 0; index--) {
                StringBuilder builder = new StringBuilder();

                builder.append(label.toLowerCase());

                for (int x = 0; x < index; x++) {
                    builder.append(".").append(args[x].toLowerCase());
                }

                String cmdLabel = builder.toString();

                if (BukkitCommandHandler.getCommandMap().containsKey(cmdLabel)) {
                    Map.Entry<Method, Object> entry = BukkitCommandHandler.getCommandMap().get(cmdLabel);

                    if (entry == null) {
                        Core.getLogger().warning("Não foi possível encontrar a entrada de " + cmdLabel);
                        return false;
                    }

                    Command command = entry.getKey().getAnnotation(Command.class);

                    if (command == null) {
                        Core.getLogger().warning("Não foi possível encontrar a anotação de Comando do " + cmdLabel);
                        return false;
                    }

                    boolean isPlayer = sender instanceof Player;

                    if (!isPlayer && command.onlyPlayer()) {
                        sender.sendMessage("§cO comando só pode ser executado por jogadores.");
                        return false;
                    }

                    if (isPlayer) {
                        Account account = Core.getAccountController().of(((Player) sender).getUniqueId());

                        if (account == null) {
                            sender.sendMessage("§cNão foi possível carregar os seus dados! ;(");
                            return false;
                        }

                        if (!account.hasRank(command.rank()) || !command.permission().isEmpty() && !account.hasPermission(command.permission())) {
                            account.send(WITHOUT_PERMISSION);
                            return false;
                        }
                    }

                    /* Rodar comando */
                    try {
                        String replacedName = label.replace(".", "");
                        int subCommand = cmdLabel.split("\\.").length - 1;

                        if (command.runAsync()) {
                            CompletableFuture.runAsync(() -> registerCommand(entry, sender, args, replacedName, subCommand));
                        } else {
                            registerCommand(entry, sender, args, replacedName, subCommand);
                        }
                    } catch (Exception e) {
                        sender.sendMessage("§cOcorreu um erro ao tentar executar este comando!");

                        Core.getLogger().log(Level.WARNING, "Não foi possível executar o comando " + command.name(), e);
                    }

                    return true;
                }
            }
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um problema ao executar o comando!", e);
        }
        return false;
    }

    private synchronized void registerCommand(Map.Entry<Method, Object> entry, CommandSender sender, String[] args, String label, int subCommand) {
        try {
            entry.getKey().invoke(entry.getValue(), new BukkitCommandContext(sender, args, label, subCommand));
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao registrar o comando...", e);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        boolean success;

        if (!holder.isEnabled()) return false;

        try {
            success = handleCommand(sender, label, args);
        } catch (Throwable exception) {
            sender.sendMessage("§cOcorreu um erro ao tentar executar este comando!");

            Core.getLogger().log(Level.WARNING, "Exceção não tratada executando comando '" + label + "' no plugin " + holder.getDescription().getFullName(), exception);
            return false;
        }

        return success;
    }

}