package com.minecraft.core.bukkit.menu.account.preference;

import com.minecraft.core.Core;
import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.event.type.account.AccountProfileChangeEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.context.elo.Elo;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.BedMetadata;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class PreferenceMenu extends Menu {

    private final Account account;
    private final Toggle toggle;
    private final BedMember bedMember;
    private int selectedTab = 0;
    private int generalPage = 1;

    private static final int TAB_GERAL = 0;
    private static final int TAB_BEDWARS = 1;
    private static final int TAB_DUELS = 2;
    private static final int TAB_STAFF = 3;

    public PreferenceMenu(Player player, Menu last) {
        super(player, "Preferências", last, 6);
        this.account = Core.getAccountController().of(player.getUniqueId());
        this.toggle = account.getToggle();
        this.bedMember = (BedMember) account.loadMember(ServerType.BEDWARS);
    }

    @Override
    public void handle() {
        clear();

        drawTabs();
        drawIndicator();
        drawContent();
        drawNavigation();

        display();
    }

    private String color(boolean enabled) {
        return enabled ? "§a" : "§c";
    }

    private void drawTabs() {
        addItem(1, tabItem(Material.PAPER, "Geral", TAB_GERAL));
        addItem(2, tabItem(Material.BED, "Bedwars", TAB_BEDWARS));
        addItem(3, tabItem(Material.DIAMOND_SWORD, "Duels", TAB_DUELS));

        if (account.isStaffer())
            addItem(4, tabItem(Material.DIODE, "Staff", TAB_STAFF));
    }

    private Item tabItem(Material material, String name, int tab) {
        boolean active = selectedTab == tab;
        return Item.of(material, active ? "§a" + name : "§7" + name,
                "", active ? "§6Selecionado atualmente." : "§eClique para selecionar")
                .click(event -> { selectedTab = tab; generalPage = 1; handle(); sound(MenuSound.SUCCESS); });
    }

    private void drawIndicator() {
        for (int i = 9; i < 18; i++)
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));

        int tabCol = switch (selectedTab) {
            case TAB_GERAL -> 1;
            case TAB_BEDWARS -> 2;
            case TAB_DUELS -> 3;
            case TAB_STAFF -> 4;
            default -> 0;
        };
        addItem(9 + tabCol, Item.of(Material.STAINED_GLASS_PANE, 5, " "));
    }

    private void drawContent() {
        switch (selectedTab) {
            case TAB_GERAL -> drawGeral();
            case TAB_BEDWARS -> drawBedwars();
            case TAB_DUELS -> drawDuels();
            case TAB_STAFF -> drawStaff();
        }
    }

    private void toggleAndRefresh(Runnable action) {
        action.run();
        account.setToggle(toggle);
        sound(MenuSound.SUCCESS);
        handle();
    }

    private void drawGeral() {
        if (generalPage == 1) {
            toggleRow(28, Material.ITEM_FRAME, "Mensagens privadas", "§7Defina se você deseja\n§7receber mensagens privadas.", toggle.isAllowMessages(), () -> toggle.setAllowMessages(!toggle.isAllowMessages()));
            toggleRow(30, Material.BOOK_AND_QUILL, "Bate-papo", "§7Exibir o bate-papo global.", toggle.isAllowFriendRequests(), () -> toggle.setAllowFriendRequests(!toggle.isAllowFriendRequests()));
            toggleRow(32, Material.BOOK, "Convites de party", "§7Alternar o recebimento\n§7de convites de party.", toggle.isAllowPartyInvite(), () -> toggle.setAllowPartyInvite(!toggle.isAllowPartyInvite()));
            toggleRow(34, Material.SIGN, "Convites de clan", "§7Alternar o recebimento\n§7de convites de clan.", toggle.isAllowClanInvite(), () -> toggle.setAllowClanInvite(!toggle.isAllowClanInvite()));
        } else {
            toggleRow(28, Material.WOOL, "Blocos coloridos", "§7Ative ou desative os\n§7blocos coloridos na hotbar.", toggle.isShowColoredBlocks(), () -> toggle.setShowColoredBlocks(!toggle.isShowColoredBlocks()));

            if (account.hasClan())
                toggleRow(32, Material.NAME_TAG, "Clantag", "§7Ative ou desative a sua\n§7clan tag no nickname.", toggle.isAllowClanTag(), () -> { toggle.setAllowClanTag(!toggle.isAllowClanTag()); TagManager.updateTag(account); });

            toggleRow(34, Material.BED, "Proteção de /lobby", "§7Ao tentar voltar ao lobby,\n§7confirme o seu retorno.", !toggle.isConfirmToHub(), () -> toggle.setConfirmToHub(!toggle.isConfirmToHub()));
        }
    }

    private void drawBedwars() {
        if (bedMember == null) return;

        BedMetadata metadata = bedMember.getMetadata();

        toggleRow(28, Material.TNT, "Temporizador da TNT", "§7Clique para ativar ou desativar\n§7o temporizador da TNT.", metadata.isTntTimerEnabled(), () -> { metadata.setTntTimerEnabled(!metadata.isTntTimerEnabled()); bedMember.saveMetadata(metadata); });

        ProfileType profile = metadata.getProfile();
        boolean isRanked = profile == ProfileType.RANKED;

        addItem(30, Item.of(isRanked ? Material.IRON_SWORD : Material.BOW, "§aEstilo de perfil",
                        "§7Altere o estilo do",
                        "§7seu perfil para outros jogadores.",
                        "",
                        "§7Usando: §a" + profile.getName(),
                        "",
                        "§eClique para alternar!")
                .flags(ItemFlag.values())
                .click(event -> {
                    ProfileType next = isRanked ? ProfileType.NORMAL : ProfileType.RANKED;
                    bedMember.setProfile(next);
                    updateProfileSidebar(bedMember, next);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(39, Item.of(Material.INK_SACK, isRanked ? 1 : 11,
                        "§aEstilo de perfil",
                        "§7Altere o estilo do",
                        "§7seu perfil para outros jogadores.",
                        "",
                        "§7Usando: §a" + profile.getName(),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    ProfileType next = isRanked ? ProfileType.NORMAL : ProfileType.RANKED;
                    bedMember.setProfile(next);
                    updateProfileSidebar(bedMember, next);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));
    }

    private void updateProfileSidebar(BedMember member, ProfileType next) {
        UserModel user = UserModel.of(member.getId());
        if (user == null) return;
        Player player = user.getAccount().player();
        if (player == null) return;

        Bukkit.getPluginManager().callEvent(new AccountProfileChangeEvent(player, next == ProfileType.RANKED ? ProfileType.NORMAL : ProfileType.RANKED, next));

        HologramClient detail = BukkitCore.getManager().getHologram().getClient(player, "stats-details");
        if (detail != null) {
            if (next == ProfileType.RANKED) {
                Elo currentElo = member.getElo();
                Elo nextElo = member.getNextElo();
                int eloXp = member.getEloXp();
                int nextMinXp = nextElo != null ? nextElo.getMinXp() : eloXp;

                detail.setText(0, "§6Rank: " + currentElo.getFullName());
                detail.setText(1, "§f["
                        + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, eloXp, nextMinXp, 10) + "§f] "
                        + "§7(" + Util.formatNumberWithLetter(eloXp) + "/" + Util.formatNumberWithLetter(nextMinXp) + ")");
            } else {
                int xp = member.getLevelXp();
                int minXp = member.getMinLevelXp();

                detail.setText(0, "Seu nível: " + member.getLevelId());
                detail.setText(1, "§f["
                        + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10) + "§f] "
                        + "§7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");
            }
        }
    }

    private void drawDuels() {
        toggleRow(30, Material.POTION, "Mostrar nível no chat", "§7Alterna a exibição de\n§7nível no chat.", toggle.isShowLevelInChat(), () -> toggle.setShowLevelInChat(!toggle.isShowLevelInChat()));
    }

    private void drawStaff() {
        toggleRow(28, Material.CHEST, "Logs", "§7Ative ou desative as\n§7logs gerais do servidor.", toggle.isAllowLogs(), () -> toggle.setAllowLogs(!toggle.isAllowLogs()));
        toggleRow(30, Material.BOOK, "Leaderboard de Punições", "§7Ative ou desative a visibilidade\n§7da leaderboard de punições.", toggle.isShowPunitionsLeaderboard(), () -> toggle.setShowPunitionsLeaderboard(!toggle.isShowPunitionsLeaderboard()));
        toggleRow(32, Material.SIGN, "Ver Reports", "§7Ative ou desative a visibilidade\n§7dos reports dos jogadores.", toggle.isShowReports(), () -> toggle.setShowReports(!toggle.isShowReports()));
        toggleRow(34, Material.DIAMOND_SWORD, "Anti-Cheat Flags", "§7Ative ou desative o recebimento\n§7de flags do anti-cheat.", toggle.isAllowACFlags(), () -> toggle.setAllowACFlags(!toggle.isAllowACFlags()));
    }

    private void toggleRow(int slot, Material material, String name, String loreText, boolean enabled, Runnable action) {
        addItem(slot, Item.of(material, color(enabled) + name,
                        loreText.split("\n"))
                .click(event -> toggleAndRefresh(action)));

        int dyeSlot = slot + 9;
        addItem(dyeSlot, Item.of(Material.INK_SACK, enabled ? 10 : 1,
                        enabled ? "§aAtivado" : "§cDesativado",
                        enabled ? "§7Essa preferência está\n§7ativa" : "§7Essa preferência está\n§7desativada",
                        "",
                        "§eClique para " + (enabled ? "desativar!" : "ativar!"))
                .click(event -> toggleAndRefresh(action)));
    }

    private void drawNavigation() {
        addItem(45, Item.of(Material.ARROW, "§aVoltar")
                .click(event -> { if (isReturnable()) getLast().handle(); }));

        if (selectedTab == TAB_GERAL) {
            addItem(50, Item.of(Material.ARROW, "§aPróxima página",
                            "",
                            "§7Página: " + (generalPage == 1 ? "§a1" : "§71") + "/§92")
                    .click(event -> { generalPage = generalPage == 1 ? 2 : 1; sound(MenuSound.SUCCESS); handle(); }));
        }
    }
}
