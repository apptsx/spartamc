package com.minecraft.core.bungee.command.structure;

import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.command.structure.annotation.BungeeCommand;
import com.minecraft.core.Core;
import com.minecraft.core.command.CommandContext;
import com.minecraft.core.command.CommandHandler;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public class BungeeCommandHandler implements CommandHandler {

    private final BungeeCore bungee;

    @Getter
    private static final Map<String, Map.Entry<Method, Object>> commands = new HashMap<>();

    @Getter
    private static final Map<String, Map.Entry<Method, Object>> completers = new HashMap<>();

    @Override
    public void handle(String path) {
        Instant now = Instant.now();

        int loaded = 0;

        Core.getLogger().info("Registrando comandos...");

        for (Class<?> cmdClass : ClassLoader.getClassesForPackage(bungee, path)) {
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
                    "(Total de comandos registrados: " + loaded + " em " + Duration.between(now, Instant.now()).toMillis() + "ms)");
    }

    @Override
    public void registerInheritor(CommandInheritor inheritor) {
        for (Method method : inheritor.getClass().getMethods()) {

            if (method.isAnnotationPresent(Command.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível registrar o comando " + method.getName() + ". Argumentos de métodos inesperados.");
                    continue;
                }

                Command command = method.getAnnotation(Command.class);

                if (command == null) break;

                for (String alias : command.aliases())
                    registerCommand(alias, method, inheritor);

                registerCommand(command.name(), method, inheritor);
            } else if (method.isAnnotationPresent(Completer.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().log(Level.SEVERE,
                            "Não foi possível registrar o completer " + method.getName() + ". Argumentos de métodos inesperados.");
                    continue;
                }

                if (method.getReturnType() != List.class) {
                    Core.getLogger().log(Level.SEVERE,
                            "Não foi possível registrar o completer " + method.getName() + ". Tipo de retorno inválido.");
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);

                for (String subCommand : completer.subCommands())
                    registerCompleter(subCommand, method, inheritor);

                registerCompleter(completer.name(), method, inheritor);
            }
        }
    }

    protected void registerCompleter(String label, Method method, Object object) {
        completers.put(label.toLowerCase(), new AbstractMap.SimpleEntry<>(method, object));
    }

    protected void registerCommand(String label, Method method, Object object) {
        Map.Entry<Method, Object> entry = new AbstractMap.SimpleEntry<>(method, object);

        commands.put(label.toLowerCase(), entry);

        String commandLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        bungee.getProxy().getPluginManager().registerCommand(bungee, new BungeeCommand(commandLabel));
    }
}