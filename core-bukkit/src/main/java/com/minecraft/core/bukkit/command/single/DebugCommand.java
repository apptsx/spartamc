package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.server.Server;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;

public class DebugCommand implements CommandInheritor {

    protected final String MEMORY_FORMAT = ChatColor.GRAY + "[%sMB/ %sMB/ %sMB/ %sMB] %s";

    @Command(name = "debug", aliases = {"dbg"}, rank = RankType.ADMIN)
    public void debug(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        int tasks = Bukkit.getScheduler().getPendingTasks().size();
        int actives = Bukkit.getScheduler().getActiveWorkers().size();

        Server server = Core.getServerData().local();

        Runtime runTime = Runtime.getRuntime();

        if (args.length == 0) {
            sender.send(
                    "§aDados gerais do servidor:",
                    "",
                    "§aNome: §f" + Bukkit.getServerName(),
                    "§aIniciado em: §f" + DateUtil.getDate(server.getPayload().getStartedAt()),
                    "",
                    "§aJogadores ativos: §f" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(),
                    "",
                    "§aTPS atual: " + UtilTPS.getFromMinutesTPS(),
                    " §aMédia: " + UtilTPS.getMedia(),
                    "",
                    "§aUso de memória: §f" + String.format(MEMORY_FORMAT, findUsedMemory(runTime),
                            findFreeMemory(runTime), findTotalMemory(runTime), findMaxMemory(runTime),
                            ChatColor.DARK_GRAY + "(" + Util.percentage((int) findUsedMemory(runTime), (int) findTotalMemory(runTime)) + "%)"),
                    "",
                    "§aTarefas pendentes: §c" + tasks,
                    "§aTarefas ativas: §f" + actives,
                    ""
            );
            return;
        }
        switch (args[0]) {
            case "lag":
            case "clear": {
                sender.send("§aExecutando limpeza...");

                Core.getPlatform().runSync(() -> {
                    System.gc();

                    sender.send("§aLimpeza de memória finalizada com sucesso.");
                });
                break;
            }
            case "tps":
            case "ticks": {
                sender.send(
                        "§aInformações de TPS:",
                        "",
                        "§aTPS atual: " + UtilTPS.getActuallyTPS(),
                        "",
                        "§aTPS: " + UtilTPS.getFromMinutesTPS(),
                        " §aMédia: " + UtilTPS.getMedia(),
                        "",
                        "§aTPS §7(Real)§a: " + UtilTPS.getRealMinutesTPS(),
                        " §aMédia §7(Real)§a: " + UtilTPS.getAverage(),
                        ""
                );
                break;
            }
            case "entity":
            case "entities": {
                sender.send("§aEntidades no servidor:");
                sender.send("");

                int entities = Bukkit.getWorlds().stream().mapToInt(earth -> earth.getEntities().size()).sum();

                sender.send("§aTotal de Entidades: §f" + entities);

                Bukkit.getWorlds().forEach(earth -> {
                    sender.send("§aEntidades em '" + earth.getName() + "': §f" + earth.getEntities().size());
                });

                sender.send("");
                break;
            }
            default: {
                sender.send("§cUso: /" + context.getLabel() + " <clear:entities:ticks>.");
                break;
            }
        }
    }

    private static final class UtilTPS {

        public static double[] findTPS() {
            return MinecraftServer.getServer().recentTps;
        }

        public static String getFromMinutesTPS() {
            return format(findTPS()[0]) + "§7(1m), " + format(findTPS()[1]) + "§7(5m), " + format(findTPS()[2]) + "§7(15m)";
        }

        public static String getRealMinutesTPS() {
            return real(findTPS()[0]) + "§7(1m), " + real(findTPS()[1]) + "§7(5m), " + real(findTPS()[2]) + "§7(15m)";
        }

        public static String getActuallyTPS() {
            return real(findTPS()[0]);
        }

        public static String getMedia() {
            return format(((findTPS()[0]) + findTPS()[1] + findTPS()[2]) / 3);
        }

        public static String getAverage() {
            return real(((findTPS()[0]) + findTPS()[1] + findTPS()[2]) / 3);
        }

        private static String format(double tps) {
            return ((tps > 18.0) ? ChatColor.GREEN : ((tps > 16.0) ? ChatColor.YELLOW : ChatColor.RED))
                    + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
        }

        private static String real(double tps) {
            return ((tps > 18.0) ? ChatColor.GREEN : ((tps > 16.0) ? ChatColor.YELLOW : ChatColor.RED))
                    + ((tps > 40.0) ? "*" : "") + new DecimalFormat("##.#").format(tps);
        }
    }

    protected static long findUsedMemory(Runtime runTime) {
        return (runTime.totalMemory() - runTime.freeMemory()) / 1048576L;
    }

    protected static long findFreeMemory(Runtime runTime) {
        return runTime.freeMemory() / 1048576L;
    }

    protected static long findTotalMemory(Runtime runTime) {
        return runTime.totalMemory() / 1048576L;
    }

    protected static long findMaxMemory(Runtime runTime) {
        return runTime.maxMemory() / 1048576L;
    }
}
