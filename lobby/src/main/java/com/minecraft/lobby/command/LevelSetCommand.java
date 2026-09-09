package com.minecraft.lobby.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.member.list.bedwars.BedMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LevelSetCommand implements CommandInheritor {

    @Command(name = "levelset", aliases = {"setlevel"}, rank = RankType.ADMIN)
    public void levelSet(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getSender().send("§cUso: /levelset <jogador> <level>");
            return;
        }

        String targetName = args[0];
        int level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            context.getSender().send("§cO level deve ser um número válido.");
            return;
        }

        if (level < 0) {
            context.getSender().send("§cO level não pode ser negativo.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null) {
            context.getSender().send("§cJogador §f" + targetName + " §cnão está online.");
            return;
        }

        Account target = Core.getAccountController().of(targetPlayer.getUniqueId());

        if (target == null) {
            context.getSender().send("§cNão foi possível encontrar a conta de §f" + targetName + "§c.");
            return;
        }

        BedMember member = Core.getBedWarsData().of(target.getId(), true);

        if (member == null) {
            context.getSender().send("§cNão foi possível encontrar os dados de BedWars de §f" + targetName + "§c.");
            return;
        }

        member.setLevel(level);
        member.setLevelXp(0);

        context.getSender().send("§aLevel de §f" + target.getNickname() + " §adefinido para §f" + member.getLevelId() + "§a.");
        target.send("§aSeu level de BedWars foi definido para §f" + member.getLevelId() + "§a.");
    }
}
