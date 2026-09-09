package com.minecraft.core.bukkit.command.structure;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.annotation.BukkitCommand;
import com.minecraft.core.bukkit.command.structure.annotation.BukkitCompleter;
import com.minecraft.core.command.CommandContext;
import com.minecraft.core.command.CommandHandler;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BukkitCommandHandler implements CommandHandler {

    private final JavaPlugin plugin;

    private final CommandMap map;

    @Getter
    private static final Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<>();

    private Map<String, org.bukkit.command.Command> knownCommands;

    public BukkitCommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;

        this.map = getCommandMap(plugin);

        try {
            Field field = map.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);

            knownCommands = (HashMap<String, org.bukkit.command.Command>) field.get(map);
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao carregar os comandos", e);
        }
    }

    private static CommandMap getCommandMap(JavaPlugin plugin) {
        try {
            Field field = plugin.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(plugin.getServer());
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível obter o CommandMap via reflection", e);
        }
    }

    @Override
    public void handle(String path) {
        Instant now = Instant.now();

        int loaded = 0;

        Core.getLogger().info("Registrando comandos...");

        for (Class<?> cmdClass : ClassLoader.getClassesForPackage(plugin, path)) {
            if (CommandInheritor.class.isAssignableFrom(cmdClass)) {
                try {
                    CommandInheritor inheritor = (CommandInheritor) cmdClass.newInstance();

                    registerInheritor(inheritor);

                    loaded++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o comando " + cmdClass.getSimpleName() + "!", e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("Registro de comandos concluído. " +
                    "(Total de comandos registrados: " + loaded + " em " + Util.formatInstant(now) + ")");
    }

    @Override
    public void registerInheritor(CommandInheritor inheritor) {
        // Registrando comandos
        for (Method method : inheritor.getClass().getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().info("Não foi possível registrar o comando " + method.getName() + ". Argumentos de método não esperados!");
                    continue;
                }

                Command command = method.getAnnotation(Command.class);
                if (command == null) {
                    continue;  // Use continue to check other methods instead of break
                }

                registerCommand(inheritor, command, command.name(), method);

                for (String alias : command.aliases()) {
                    registerCommand(inheritor, command, alias, method);
                }
            }
        }

        // Registrando completers
        for (Method method : inheritor.getClass().getMethods()) {
            if (method.isAnnotationPresent(Completer.class)) {
                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().info("Não foi possível registrar o completer " + method.getName() + ". Argumentos de método não esperados!");
                    continue;
                }

                if (!List.class.isAssignableFrom(method.getReturnType())) {
                    Core.getLogger().info("Não foi possível registrar o completer " + method.getName() + ". Tipo de retorno inesperado. O retorno deve ser uma Lista de Strings.");
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);
                if (completer == null) {
                    continue;  // Use continue to check other methods instead of break
                }

                for (String subCommand : completer.subCommands()) {
                    registerCompleter(inheritor, method, subCommand);
                }

                registerCompleter(inheritor, method, completer.name());
            }
        }
    }

    private void registerCommand(CommandInheritor base, Command command, String label, Method method) {
        Map.Entry<Method, Object> entry = new AbstractMap.SimpleEntry<>(method, base);

        commandMap.put(label.toLowerCase(), entry);

        String commandLabel = label.split("\\.")[0].toLowerCase();

        if (map.getCommand(commandLabel) == null) {
            BukkitCommand cmd = new BukkitCommand(plugin, command.rank(), commandLabel);

            map.register(commandLabel, cmd);
        } else if (map.getCommand(commandLabel) instanceof BukkitCommand) {
            BukkitCommand bukkitCommand = (BukkitCommand) map.getCommand(commandLabel);

            if (bukkitCommand != null)
                bukkitCommand.setRank(command.rank());
        }
    }

    private void registerCompleter(CommandInheritor base, Method method, String label) {
        String cmdLabel = label.split("\\.")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(plugin, RankType.MEMBER, cmdLabel);

            knownCommands.put(cmdLabel, command);
        }

        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);

            if (command.getCompleter() == null) {
                command.setCompleter(new BukkitCompleter());
            }

            command.getCompleter().addCompleter(label, method, base);
        }

        if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                org.bukkit.command.Command command = map.getCommand(cmdLabel);

                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);

                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();

                    completer.addCompleter(label, method, base);
                    field.set(command, completer);
                }

                if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);

                    completer.addCompleter(label, method, base);
                } else {
                    Core.getLogger().info("Não foi possível registrar o completer " + method.getName()
                            + ". Um completer já está registrado para esse comando!");
                }

            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível registrar o completador...", e);
            }
        }
    }
}
