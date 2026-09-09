package com.minecraft.core.bukkit.api.vanish;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.bukkit.event.type.account.AccountVanishLogEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Vanish {

    @Getter
    private static final Set<Vanish> list = new HashSet<>();

    private final Player player;

    public Vanish(Player player) {
        this.player = player;

        list.add(this);
    }

    public static void handle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (!has(player)) {
            hideAndShow(player, AccountVanishLogEvent.VanishState.JOIN);
            
            TitleCollectible.updateTitleVisibility(player, false);
            
            if (account.getToggle() != null) {
                account.getToggle().setShowColoredBlocks(false);
                account.setToggle(account.getToggle());
            }

            account.send("",
                    "§dVocê entrou no modo vanish.",
                    "§dAgora você está invisível para " + account.getRankType().bellow().getColoredName() + "§d e abaixo.",
                    "");

            player.setGameMode(GameMode.CREATIVE);
            player.playSound(player.getLocation(), Sound.FALL_BIG, 1.0f, 2.0f);

            new Vanish(player);
        } else {
            hideAndShow(player, AccountVanishLogEvent.VanishState.LEAVE);
            
            TitleCollectible.updateTitleVisibility(player, true);
            
            if (account.getToggle() != null) {
                account.getToggle().setShowColoredBlocks(true);
                account.setToggle(account.getToggle());
            }

            account.send("",
                    "§cVocê saiu do modo vanish.",
                    "§cAgora você está visível para todos os jogadores.",
                    "");

            player.setGameMode(GameMode.SURVIVAL);
            player.playSound(player.getLocation(), Sound.BAT_DEATH, 1.0f, 2.0f);

            remove(player);
        }

        new AccountVanishLogEvent(account, has(player) ? AccountVanishLogEvent.VanishState.JOIN : AccountVanishLogEvent.VanishState.LEAVE).call();
    }

    public static boolean has(Player player) {
        return list.stream().anyMatch(vanish -> vanish.getPlayer().getUniqueId().equals(player.getUniqueId()));
    }

    public static void remove(Player player) {
        list.removeIf(vanish -> vanish.getPlayer().getUniqueId().equals(player.getUniqueId()));
    }

    public static void hideAndShow(Player player, AccountVanishLogEvent.VanishState state) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (state == AccountVanishLogEvent.VanishState.LEAVE) {
            Bukkit.getOnlinePlayers().forEach(target -> target.showPlayer(player));
        } else {
            Core.getAccountController()
                    .filter(target -> target.player() != null && !target.hasRank(account.getRank()))
                    .forEach(target -> target.player().hidePlayer(player));
        }
    }
}