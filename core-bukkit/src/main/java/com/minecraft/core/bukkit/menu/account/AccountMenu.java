package com.minecraft.core.bukkit.menu.account;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.account.friend.FriendMenu;
import com.minecraft.core.bukkit.menu.account.medal.MedalMenu;
import com.minecraft.core.bukkit.menu.account.skin.library.SkinLibraryCategoryMenu;
import com.minecraft.core.bukkit.menu.account.stats.StatsMenu;
import com.minecraft.core.bukkit.menu.account.preference.PreferenceMenu;
import com.minecraft.core.bukkit.menu.server.clan.ClanMenu;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AccountMenu extends Menu {
    private final Account account;

    public AccountMenu(Player player) {
        super(player, "Seu perfil", 6); // 6 linhas

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        // Skull do jogador - slot 14 (no código é 13)
        addItem(13, Item.of(Material.SKULL_ITEM, 3, "§a" + account.getName(),
                        "§7Rank: " + account.getRank().getColoredName(),
                        "§7Primeiro login: " + DateUtil.getSimpleDate(account.getContext().getCreatedAt()),
                        "§7Último login: " + DateUtil.getSimpleDate(account.getContext().getLastLogin()))
                .skullByBase64(account.getSkin().getValue()));

        // Skins - slot 30 (no código é 29)
        addItem(29, Item.of(Material.ITEM_FRAME, "§aSkins")
                .click(event -> {
                    new SkinLibraryCategoryMenu(getPlayer(), this).handle();
                }));

        // Estatísticas - slot 31 (no código é 30)
        addItem(30, Item.of(Material.PAPER, "§aEstatísticas")
                .click(event -> {
                    new StatsMenu(getPlayer(), account, this).handle();
                }));

        // Preferências - slot 32 (no código é 31)
        addItem(31, Item.of(Material.DIODE, "§aPreferências")
                .click(event -> {
                    new PreferenceMenu(getPlayer(), this).handle();
                }));

        // Amizades - slot 33 (no código é 32)
        addItem(32, Item.of(Material.COOKIE, "§aAmizades")
                .click(event -> {
                    new FriendMenu(getPlayer(), this).handle();
                }));

        // Medalhas - slot 34 (no código é 33)
        addItem(33, Item.of(Material.DOUBLE_PLANT, "§aMedalhas")
                .click(event -> {
                    new MedalMenu(getPlayer(), this).handle();
                }));

        // Nickname - slot 40 (abaixo de Estatísticas)
        Item nickItem = Item.of(Material.NAME_TAG, "§aNick (/nick)", "§7Escolha um disfarce", "§7para usar no servidor!", "", "§7Exclusivo para §5§lMAX§6§l+", "", "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new com.minecraft.core.bukkit.menu.account.nickname.NicknameMenu(getPlayer(), this).handle();
                });
        addItem(39, nickItem);

        // Menu do Max+ - slot 42 (abaixo de Amizades)
        boolean isMaxPlus = account.getRankType().ordinal() >= RankType.MAX_PLUS.ordinal();
        Item maxPlusItem = Item.of(Material.INK_SACK, 9, "§aMenu do §5Max§6+",
                "§7Confira as customizações do Max+,", "", "§7Exclusivo para §5§lMAX§6§l+", "", "§eClique para ver mais!")
                .click(event -> {
                    if (isMaxPlus) {
                        new MaxPlusColorMenu(getPlayer(), this).handle();
                    } else {
                        getPlayer().performCommand("loja");
                    }
                });
        addItem(41, maxPlusItem);

        display();
    }
}