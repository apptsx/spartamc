package com.minecraft.core.bukkit.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.server.arcade.custom.CustomArcadeMenu;
import com.minecraft.core.bukkit.menu.server.arcade.info.ArcadeInfoMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.share.ShareMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.FavoriteItemShare;
import com.minecraft.core.util.list.Validator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArcadeCommands implements CommandInheritor {

    @Command(name = "arcade", rank = RankType.ADMIN, onlyPlayer = false)
    public void arcade(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        if (!Core.getServerType().isArcade()) {
            sender.send("§cO servidor atual não suporta jogos arcade.");
            return;
        }

        if (!sender.isPlayer()) {
            Set<ArcadeHolder> arcadeList = BukkitCore.getManager().getArcade().getArcades();

            List<Room> roomList = BukkitCore.getManager().getArcade().getArenas();

            sender.send(
                    "",
                    "§eInformações de jogos:",
                    "§eJogos disponíveis: §b" + arcadeList.size(),
                    "§eTotal de salas: §b" + roomList.size(),
                    "");

            for (ArcadeHolder arcade : arcadeList) {
                Set<Room> rooms = arcade.getRooms();

                int occupiedRooms = (int) rooms.stream().filter(room -> !room.isAvailable()).count(),
                        availableRooms = (rooms.size() - occupiedRooms);

                sender.send("§eJogo: §b" + arcade.getName(),
                        "§eSalas atuais: §b" + rooms.size(),
                        " §eOcupadas: §c" + occupiedRooms + " §eDisponíveis: §a" + availableRooms);
            }

            sender.send("");
        } else
            new ArcadeInfoMenu(context.getPlayer()).handle();
    }

    @Command(name = "salacustom", rank = RankType.ADMIN)
    public void customRoom(BukkitCommandContext context) {
        new CustomArcadeMenu(context.getPlayer(), null).handle();
    }

    @Command(name = "arcadedev", aliases = {"acd"}, rank = RankType.ADMIN)
    public void acd(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (jogo)");
            return;
        }

        ArcadeCategory arcade = ArcadeCategory.of(context.getMessage(0, args));

        if (arcade == null) {
            account.send("§cO jogo selecionado não foi encontrado.");
            return;
        }

        account.send("§aEnviando...");

        account.redirect(ArcadeRouteContext.builder()
                .arcade(arcade)
                .join(Join.PLAYER)
                .slot(arcade.getSlots().get(0))
                .build());
    }

    @Command(name = "share")
    public void shareBedWars(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (id).");
            return;
        }

        if (!Validator.isValidUUID(args[0])) {
            account.send("§cO ID informado não é válido.");
            return;
        }

        UUID uuid = UUID.fromString(args[0]);

        Account target = context.getAccount(uuid);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        FavoriteItemShare share = FavoriteItemShare.of(target.getId(), account.getId());

        if (share == null) {
            account.send("§cO jogador " + target.getNickname() + " não compartilhou os seus favoritos com você.");
            return;
        }

        new ShareMenu(context.getPlayer(), share).handle();
    }
}
