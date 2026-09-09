package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.core.bukkit.user.UserModel;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagManager {

    public static List<Tag> getAvailableTags(Account account) {
        return Arrays.stream(Tag.values())
                .filter(account::hasTag)
                .collect(Collectors.toList());
    }

    protected static TextComponent create(Account account, Tag tag, boolean end) {
        TextComponent message = new TextComponent(tag.getColoredName());

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                "§7Prévia:"
                        + "\n§8> " + tag.getByPrefix(account.getTagPrefix()) + account.getNickname()
                        + "\n\n" + (account.isUsingTag(tag) ? "§cJá selecionada." : "§eClique para escolher.")
        )));

        message.addExtra("§r" + (end ? "." : ", "));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag " + tag.getName().toLowerCase()));

        return message;
    }

    public static void sendTags(Account account) {
        List<Tag> tags = getAvailableTags(account);

        TextComponent message = new TextComponent("§aSuas tags: ");

        boolean end = false;

        int index = 1;
        for (Tag tag : tags) {
            if (index >= tags.size())
                end = true;

            message.addExtra(create(account, tag, end));
            index++;
        }

        message.addExtra(new TextComponent("§r"));

        account.send(message);
    }

    public static void updateTag(Account account) {
        updateTag(account, UserModel.has(account.getId()) ? UserModel.of(account.getId()).getTag() : account.getTag());
    }

    public static void updateTag(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null)
            updateTag(account);
    }

    public static void updateTag(Account account, Tag tag) {
        Player player = account.player();

        if (player == null) return;

        if (tag == null) tag = Tag.MEMBER;

        String order = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        // Se for CHEFE, iniciar animação
        if (tag.equals(Tag.CHEFE)) {
            ChefeTagAnimation.start(player);
        } else {
            ChefeTagAnimation.stop(player);
        }

        String prefix;
        if (tag.equals(Tag.MAX_PLUS)) {
            // Tag MAX+: formato padrão §5§lMAX§<cor>§l+ §5
            char colorChar = account.getMaxPlusColor();
            int level = account.getMaxPlusLevel();
            
            if (level == 1 || level == 0) {
                // Nível 1 (padrão): MAX+ com cor selecionada para o +
                prefix = "§5§lMAX§" + colorChar + "§l+ §5";
            } else if (level == 2) {
                // Nível 2: MAX+ azul com cor selecionada para o +
                prefix = "§b§lMAX§" + colorChar + "§l+ §b";
            } else if (level == 3) {
                // Nível 3: MAX+ dourado brilhante com cor selecionada para o +
                prefix = "§6§lMAX§" + colorChar + "§l+ §6";
            } else {
                // Fallback
                prefix = "§5§lMAX§" + colorChar + "§l+ §5";
            }
        } else {
            prefix = getAdjustedPrefix(account.getTagPrefix(), tag);
        }

        String suffix = !account.isUsingFake() ? (account.hasClan() && account.getToggle().isAllowClanTag() ? account.getClanTag() : "") : "";
        suffix = truncateSuffix(suffix);

        // Limpar equipes antigas do jogador
        for (Team old : player.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:")) {
                if (old.hasEntry(player.getName())) {
                    old.unregister();
                }
            }
        }

        Team team = createTeamIfNotExists(order, player, player.getName(), prefix, suffix);
        Scoreboard scoreboard = team.getScoreboard();

        String displayName = team.getPrefix() + player.getName() + team.getSuffix();
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
        player.setScoreboard(scoreboard);

        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            // Exibe a tag do jogador `player` para `onlinePlayer`
            applyTagForViewer(player, onlinePlayer, tag);

            // Exibe a tag do `onlinePlayer` para o `player`
            Tag onlineTag = UserModel.of(onlinePlayer.getUniqueId()).getTag();

            applyTagForViewer(onlinePlayer, player, onlineTag);
        }
    }

    private static void applyTagForViewer(Player player, Player viewer, Tag tag) {
        UserModel viewerModel = UserModel.of(viewer.getUniqueId());
        if (viewerModel == null) return;

        Account viewerAccount = viewerModel.getAccount();

        // Definindo o prefixo do viewer com base no TagPrefix do visualizador
        TagPrefix viewerPrefixType = viewerAccount.getTagPrefix();
        String viewerPrefix = getAdjustedPrefix(viewerPrefixType, tag);

        // Verifica a própria tag de clã do viewer
        String viewerClanTag = !viewerAccount.isUsingFake() ? (viewerAccount.hasClan() && viewerAccount.getToggle().isAllowClanTag()
                ? viewerAccount.getClanTag()
                : "") : "";
        viewerClanTag = truncateSuffix(viewerClanTag);

        // Verifica a tag de clã do player
        Account playerAccount = Core.getAccountController().of(player.getUniqueId());
        String playerClanTag = playerAccount != null && !playerAccount.isUsingFake() ? (playerAccount.hasClan() && playerAccount.getToggle().isAllowClanTag()
                ? playerAccount.getClanTag()
                : "") : "";
        playerClanTag = truncateSuffix(playerClanTag);

        // Gerar ordem para o time específico do visualizador
        String viewerOrder = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        // Removendo times antigos para o visualizador
        for (Team old : viewer.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:") && old.hasEntry(player.getName())) {
                old.unregister();
            }
        }

        // Criando ou obtendo a equipe com a tag ajustada para o visualizador, com a tag de clã do player
        Team viewerTeam = createTeamIfNotExists(viewerOrder, viewer, player.getName(), viewerPrefix, playerClanTag);

        // Adicionando o jogador (player) à equipe com a visualização do viewer
        if (!viewerTeam.hasEntry(player.getName())) {
            viewerTeam.addEntry(player.getName());
        }
        
        // Garantir que o prefixo está atualizado (importante para o tab list)
        viewerTeam.setPrefix(viewerPrefix != null ? viewerPrefix : "");
        viewerTeam.setSuffix(truncateSuffix(playerClanTag));

        // Adicionando a própria tag de clã do viewer
        viewer.setDisplayName(viewerPrefix + viewer.getName() + viewerClanTag);
    }

    private static Team createTeamIfNotExists(String order, Player player, String entry, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard()))
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team team = scoreboard.getTeam(order);
        if (team == null) {
            team = scoreboard.registerNewTeam(order);
        }

        team.setCanSeeFriendlyInvisibles(false);
        // Sempre atualiza o prefixo e sufixo, mesmo se o Team já existir
        team.setPrefix(prefix != null ? prefix : "");
        team.setSuffix(truncateSuffix(suffix));

        if (!team.hasEntry(entry))
            team.addEntry(entry);

        return team;
    }

    private static String getAdjustedPrefix(TagPrefix prefixType, Tag tag) {
        String prefix = tag.getByPrefix(prefixType);

        if (prefix.length() > 16) {
            StringBuilder result = new StringBuilder();
            int totalChars = 0;
            int i = 0;

            while (i < prefix.length() && totalChars < 16) {
                char c = prefix.charAt(i);

                if (c == '§' && i + 1 < prefix.length()) {
                    if (totalChars + 2 <= 16) {
                        result.append(c);
                        result.append(prefix.charAt(i + 1));
                        totalChars += 2;
                        i += 2;
                    } else {
                        break;
                    }
                } else {
                    result.append(c);
                    totalChars++;
                    i++;
                }
            }

            prefix = result.toString();
        }

        return prefix;
    }

    private static String truncateSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) return suffix;
        
        // O protocolo do Minecraft limita suffix a 16 caracteres totais (incluindo códigos de cor)
        if (suffix.length() <= 16) {
            return suffix;
        }
        
        // Truncar para 16 caracteres, tentando manter códigos de cor completos
        StringBuilder result = new StringBuilder();
        int totalChars = 0;
        int i = 0;
        
        while (i < suffix.length() && totalChars < 16) {
            char c = suffix.charAt(i);
            
            if (c == '§' && i + 1 < suffix.length()) {
                // É um código de cor, verificar se cabe
                if (totalChars + 2 <= 16) {
                    result.append(c);
                    result.append(suffix.charAt(i + 1));
                    totalChars += 2;
                    i += 2; // Pular o próximo caractere
                } else {
                    // Não cabe mais, parar
                    break;
                }
            } else {
                // É um caractere normal
                if (totalChars < 16) {
                    result.append(c);
                    totalChars++;
                    i++;
                } else {
                    // Limite atingido, parar
                    break;
                }
            }
        }
        
        return result.toString();
    }

    public static void removeTag(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        player.getScoreboard().getTeams().stream()
                .filter(team -> team.getName().startsWith("tag:"))
                .forEach(Team::unregister);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player) || onlinePlayer.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard()))
                continue;

            onlinePlayer.getScoreboard().getTeams().forEach(team -> removeEntries(team, player.getName()));
        }
    }

    private static void removeEntries(Team team, String entry) {
        if (team.getName().startsWith("tag:")) {
            team.removeEntry(entry);

            if (team.getEntries().isEmpty())
                team.unregister();
        }
    }
}