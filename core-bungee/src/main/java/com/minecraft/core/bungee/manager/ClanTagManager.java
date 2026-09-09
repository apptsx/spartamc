package com.minecraft.core.bungee.manager;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.api.clan.color.ClanColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ClanTagManager {

    public static boolean canUseColor(Account account, ClanColor color) {
        // Cinza é a cor padrão, disponível para todos
        if (color == ClanColor.CINZA) {
            return true;
        }

        RankType rank = account.getRank().getType();

        if (rank == null) rank = RankType.MEMBER;

        // Admin tem todas as cores
        if (rank == RankType.ADMIN || rank == RankType.CHEFE) {
            return true;
        }

        // Mod ou superior tem acesso ao Roxo
        if (rank.isHigherOrEqual(RankType.MOD) && color == ClanColor.ROXO) {
            return true;
        }

        // Partner+ ou superior tem acesso ao Ciano (§3)
        if (rank.isHigherOrEqual(RankType.PARTNER_PLUS) && (color == ClanColor.CIANO || color == ClanColor.CIANO_CLARO)) {
            return true;
        }

        // Max+ ou superior tem acesso ao Rosa
        if (rank.isHigherOrEqual(RankType.MAX_PLUS) && color == ClanColor.ROSA) {
            return true;
        }

        // Permissões personalizadas (via comando /acc clantag add)
        return account.hasClanTagColorPermission(color.getCode());
    }

    public static ClanColor getDefaultColor() {
        return ClanColor.CINZA;
    }

    public static boolean canUseColorForClan(Account account, Clan clan, ClanColor color) {
        // Se o clan já tem uma cor especial definida (diferente das cores padrão do jogador), permitir
        if (clan.getTagColor() != null && clan.getTagColor().equals(color.getCode())) {
            return true;
        }

        return canUseColor(account, color);
    }

    public static List<ClanColor> getAvailableColors(Account account) {
        List<ClanColor> available = new ArrayList<>();

        for (ClanColor color : ClanColor.values()) {
            if (canUseColor(account, color)) {
                available.add(color);
            }
        }

        return available;
    }

    public static TextComponent createColorOption(Account account, Clan clan, ClanColor color, boolean end) {
        String displayName = color.getCode() + color.getName();
        String currentColor = clan.getTagColor();

        String preview = "§7Prévia: " + color.getCode() + clan.getName() + " §7(" + clan.getName() + ")";
        String action = currentColor != null && currentColor.equals(color.getCode())
                ? "§aJá selecionada."
                : "§eClique para selecionar.";

        TextComponent message = new TextComponent(displayName + (end ? "" : ", "));

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                preview + "\n\n" + action
        )));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan tag " + color.getName().toLowerCase().replace(" ", "_")));

        return message;
    }

    public static void sendColorMenu(Account account, Clan clan) {
        List<ClanColor> availableColors = getAvailableColors(account);

        if (availableColors.isEmpty()) {
            account.send("§cVocê não possui nenhuma cortag! :(");
            account.send("§cCompre em nossa loja: §ehttps://" + com.minecraft.core.Constant.SERVER_STORE);
            return;
        }

        TextComponent message = new TextComponent("§aSuas cores de ClanTag: §7");

        for (int i = 0; i < availableColors.size(); i++) {
            ClanColor color = availableColors.get(i);
            boolean end = (i == availableColors.size() - 1);

            TextComponent colorComponent = new TextComponent(color.getCode() + color.getName() + (end ? "" : "§7, "));

            String preview = "§7Prévia: " + color.getCode() + clan.getName();
            String action = clan.getTagColor() != null && clan.getTagColor().equals(color.getCode())
                    ? "§aJá selecionada."
                    : "§eClique para selecionar.";

            colorComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                    preview + "\n\n" + action
            )));
            colorComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan tag " + color.getName().toLowerCase().replace(" ", "_")));

            message.addExtra(colorComponent);
        }

        account.send(message);
    }

    public static void applyColor(Clan clan, ClanColor color) {
        clan.setTagColor(color.getCode());
    }
}
