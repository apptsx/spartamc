package com.minecraft.lobby.architect.list.bedwars;

import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedFinalKillLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedLevelLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedRankLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedWinstreakLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedWinsLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedBedDestructionLeaderboard;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedModeSelectorHologram;
import com.minecraft.lobby.architect.list.bedwars.leaderboard.BedPeriodSelectorHologram;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.account.AccountProfileChangeEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.BedWarsMenu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.BedCollectibleController;
import com.minecraft.core.member.context.elo.Elo;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.menu.navigation.bedwars.BedNavigationMenu;
import com.minecraft.lobby.menu.navigation.bedwars.BedVersusMenu;
import com.minecraft.lobby.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BedWarsArchitect extends Architect {

    public BedWarsArchitect() {
        super(ServerType.HUB_BEDWARS);
    }

    @Override
    public void load() {
        super.load();
        Core.getCollectibleController().handle(Lobby.getInstance(), "com.minecraft.core.api.collectible");
        BedCollectibleController.handle(Lobby.getInstance());
    }

    @Override
    public void unload() {
        BedCollectibleController.disable();
    }

    @Override
    public void join(Player player) {
        UUID id = player.getUniqueId();

        BedMember member = Core.getBedWarsData().of(id, true);

        if (member == null)
            member = Core.getBedWarsData().save(new BedMember(id, player.getName()));

        Core.getMemberController().save(member);
        Core.getBedWarsData().cancelExpiration(id);

        User user = (User) User.of(player.getUniqueId());
        super.join(player);

        Core.getPlatform().runSync(() -> handleLeaderboards(user), 20L);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getBedWarsData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        BedMember member = user.getMember(BedMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§6§lBED WARS");
        sidebar.blankRow();

        if (member.isProfile(ProfileType.RANKED)) {
            Elo currentElo = member.getElo();
            Elo nextElo = member.getNextElo();
            int eloXp = member.getEloXp();
            int nextMinXp = nextElo != null ? nextElo.getMinXp() : eloXp;

            sidebar.addRow("rank", "Rank: " + currentElo.getFullName());

            sidebar.addRow("progress", " §8[" + Util.createProgressBar(
                    ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, eloXp, nextMinXp, 10, false) + "§8]"
                    + " §7(" + Util.formatNumberWithLetter(eloXp) + "/" + Util.formatNumberWithLetter(nextMinXp) + ")");
        } else {
            int xp = member.getLevelXp();
            int minXp = member.getMinLevelXp();

            sidebar.addRow("rank", "Seu nível: " + member.getLevelId());

            sidebar.addRow("progress", " §8[" + Util.createProgressBar(
                    ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10, false) + "§8]"
                    + " §7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");
        }

        sidebar.blankRow();

        sidebar.addRow("solo", "§eSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.BEDWARS_SOLO)));
        sidebar.addRow("solo_winstreak", " Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.BEDWARS_SOLO)));

        sidebar.addRow("duo", "§eDuplas:");
        sidebar.addRow("duo_wins", " §fVitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.BEDWARS_DUO)));
        sidebar.addRow("duo_winstreak", " §fWinstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.BEDWARS_DUO)));

        sidebar.blankRow();
        sidebar.addRow("players", "Players: §b" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleHotbar(Player player) {
        super.handleHotbar(player);

        Core.getPlatform().runSync(() -> {
            if (!player.isOnline()) return;
            
            PlayerInventory inv = player.getInventory();
            inv.setItem(2, Item.of(Material.EMERALD, "§aMenu do " + ServerType.BEDWARS.getName())
                    .interact(event -> new BedWarsMenu(event.getPlayer()).handle()));
            
            player.updateInventory();
        }, 3L);
    }

    @Override
    public void handleEntities() {
        super.handleEntities();

        Location statsLoc = getLocation("npc_stats");
        Location soloLoc = getLocation("npc_solo");
        Location duoLoc = getLocation("npc_duo");
        Location trioLoc = getLocation("npc_trio");
        Location quartetLoc = getLocation("npc_quartet");
        Location versusLoc = getLocation("npc_versus");

        if (statsLoc != null) {
            handleStatsNpc(statsLoc);
        }

        if (soloLoc != null) {
            handleArcadeNpc(ArcadeCategory.BEDWARS_SOLO, soloLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxOTI5NDc0NjQ5NiwKICAicHJvZmlsZUlkIiA6ICJjMGYzYjI3YTUwMDE0YzVhYjIxZDc5ZGRlMTAxZGZlMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDEzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmNDFlNDIwMmI4YTFhMTMzZjBiYjVlZWFhM2IyZTA2NmJiZWYzOTZlNTkxMTVkODg2OTliZTE5NTlkY2M3MTciCiAgICB9CiAgfQp9",
                    "RD0qdkGjt0Dl7VD2zq9gM0BuZPcRxrXyQG8YEXxyZVdw6pbDt/v51PeONCG42RutEGjCWgsu31UWnqjEDMXKT8dAfELUtZtJmEkYosm73037oLFY8jMvJIwrw+sOIOVIQXg+jzyo6/ySAxj217yLDA7eXfWYIPWYD8Nnd/CfojlcsgrKtYytSoy5py4g+AjX5P+v15kEHP/NxGxmULo7Fa4EFUInoNh0B0ZEi7fDa66LnnLekaeCDdD+PqWihnosq4r88/GJSPgRGJbfLTEwN/WHZqGMVoOgKEuSu7x3emOQFU4/Hw26KdOtHdFXAFNNwVeDjD8JU9+Sd/bpD74K9zjDNJaPSgiGld3San0vQHEUEstF8CffpUCNwR5EdX3fRAaNmPPG2OaNre0IylWrH6FeivZ6iDQjnKV2zNJh9+0GPtmwiwjwO28Sn4jhAbmo90oWiZbxuXbem/x4UEpKCrgeYo+gNkwW1+fYZszNwca9IPb+YSxR/K2WyzYLaA5NYcevAwuuPhbVFRBa0fVySN1KAav4ReByBQMV+olgDZNJWqBxfK9h/W1WFASp0wwLDD5GFWqeEoUoPfFMXOrRDjET6uLXl7tsi7iEcdWtrwxAh4b0D3siYQ6cNaoYNuSMa1mpsDBaPYGbKwXJNl+ViTSqJQKPcKvCDMhCuiVe9lM=");
        }

        if (duoLoc != null) {
            handleArcadeNpc(ArcadeCategory.BEDWARS_DUO, duoLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcxMzE4NjgzMTUxOCwKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFkOWVhOTRjOTU3ZWMyOGU2YzVhM2U1YzlkNGQ3NmEzMzU0ZjQ2MDE5ZTZhOGUwNTViZTYyMmJlYmMwNjY5ZDQiCiAgICB9CiAgfQp9",
                    "maG14gs+bGf28lxQ4ZbQxmcf6xmhhCWqL292YfNyFWHyDvxXGLBCApcGIFH/vdhe1Jdfj2M2Yciz27nLn82wWvZz+Cn9ETfKeNHxmrDBkYbuRTOU/7XOvDtIfq5MQTSXeH69ZLpr2N72GxCLfApfEEi87gkoJibSLxDMZB7rMcso9v+liAV/Cw0SCF8Mwx2KsQxkg84uRpvANr+eO5vFp+BgENT41NPCDTYw74iQbQB4n6fU7I3Oqkiuo9BiLsGr+ZBmgwyCtfjH/E1TgQma/JMobBsBMqm4lh0oYcS0u95ffdBQphd9FKr2hStfpSwBcHQOKNYpIS6ZLQP6/be6+jmHjH5CUGUEYoVXNWcBFCzjFsPRbyHuZJSnCzxHOgr7MLJXo14YGOFsapUJ4n4KarzhccWpZ1InkG8ABc3ClHKmSonMEftH4gck0FH/AzEUO0OCAEdaYc2xyHGuTfijdL/B2g4kLVlKuafdlF31f6USTQHK1SVSX8RIqyxGyyBXr81Y45Zc7v1dk/FsiyrTat8pzF9iLwMpkxHN1oJpwU/9o8acbth92bfRU6QdHL+Ad2/AsONbHWSV4GKygSV/INoFqvH6CtjtS2Z3icGU1TaK/jT5Dcv8Oq//0QXGDXPIFhAbcQIKRERftH4Ai7En/pvvHVqw02Gw9DvnvWRGWz0=");
        }

        if (trioLoc != null) {
            handleArcadeNpc(ArcadeCategory.BEDWARS_TRIO, trioLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyNTQ4MjEyOTE2NywKICAicHJvZmlsZUlkIiA6ICIzZDU1OGQ3Y2NmZjk0ODdkYWE1MzhkMjM4NGE3OWFkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcnlwdGljTG9zZXIxMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNjJiMjM5MjA3YjcxN2UxZDZkMzhhMzRkNmM2Y2EzYzg4YzJkOWEyZmE0NTQ1YjM0MWNiYzNlMjBjMjBhM2E0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "f0VAeuolcsQrNQEmS/l/HkAukjLWw7sRfJaJH317Bt4iEvCt6cf9LBfbqm/eyHtVH6yOxB+8nC9xcZDtBp2WTpmDpOQegEPYWYbn7RcfKchuJ2JErVG8Di3K4NQTr3wSCoSDOuwQCjJ0UCxVsocrOa4y4dfWudS8lj4Qy+aexkwzdAJg1Che51btv3diZ8G9ILx9ARK2E6H0enoX2kelVj8imYmBOSotGU1E+CVcbp+Sfm0TTRGvl44VmVvGBLP4vjVGd+exMvqIGZjC2t+8T7GNlAy5lfWXwY/mFvs6q5r0yIIj4rCcNOxpU+lg3k+2occnvd1Qfg3eDF3PYVBuSAP/n1ncmlTnPzEsWim4hsWUHNeETL46sfw13tlMTNEHnHY0StxS9HJS373omBNj8tT7meH65hhykpJdwWylooBAeaHUAOQxmfgTt68hX/7+fpdOu/vUaOgRa6Cq9kTg20n2ow065+TiOiaBnZ5J8B2evTLii5kumpWwKnXRpJP455m2SUNppWH1AAZ63ZtNdg6oUe5aLmO5W+Ps6cN4Kn2PMg5wVd7wqZ0jlxw9/qTPemrkxSFguwA8h6ZyzLQsIk5teBp+GNeSJBFa2pTCQJU8hK1Av8ctZfLsorj59JD620rTgdI7ST4Dcub4xjict4MICirCjR1oyhbtVmlPg/0=");
        }

        if (quartetLoc != null) {
            handleArcadeNpc(ArcadeCategory.BEDWARS_QUARTET, quartetLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxMTMyNzMxMDE3NCwKICAicHJvZmlsZUlkIiA6ICJmNWQwYjFhZTQxNmU0YTE5ODEyMTRmZGQzMWU3MzA1YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRjaFRoZVdhdmUxMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZjFmZTQxY2FkODVmYWU3MzliNGZkMDk4NzQ4NTAxMWQ3NTA3MzFjZDViNjU2NmE0ZjVhOWY0YTliMjU2ZTlhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "smmVV+Zx+XsHATxU4MIiMUYnhAPE3qMzm9+VPYWkgzKeZyPYAVNPCxs5GJep7AVZw9eZvcPhqF6nfL/Xrbf8NP20qju7guEPYi3JfZBU/QKjbKgIoPjMgJSUlEvR5bhYuTSz2pU2ICZA3GvbAf/pb8dO5wR1tgi3B07BYQqLOJDJZwd2xjElHmbDLCUrx6fWzo19AS2Ku5Uglcu6BS07uGwo5z3x4DsUQXn5uxzqMU0hZmXi/nQFjfs3BQz/Ge5TdhnI/SwhefME1iQkVe3aUDTmCMBvdkqS7tbNBemcvH3PgkAmQg/JzTP/5Z4pkGz9EPZsOwtu31SXhYtT5V+OWn+vlwhlCdWEcQFDd+jG37ZS8Z9a/QahfDLmoA2VC1nxl/aDXDEosbuV9uNnOJCsdjHm+rgGdpUkPBdU6DPlQLYri60B1fAYq4i9HQO/EyB8hPk5Vr2Og/PsBWiW3YiJhmrws6rnFCdvZPoefMaypW4N9qhS7oSjH3A/FGtRN19QyHC6GDdUnb15YZGxzzW0ZkgdOemdFF6DYeCpmVaK7FJQg64E/8zTeI5h3jqqsZsBCs45v8X9wFL94oVauAnXWb1FJ7KTAzlLke9C91W2+PkcWiXWVXmQ/eSCpJapksZwJiNucraLbat8GqgENGYeX1e/zK1YZ5hrICSLBGGZfzE=");
        }

        if (versusLoc != null) {
            NpcServer versus = handleNpc("Duels", versusLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYyNjcwNDg1ODc2NSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFjNGE4MjlhODg0M2I0ZTgzOWIwMGY2ZmE3OTlkMWFiYjYzN2NhMjU0Y2MwNTRhYTcyNjUwZTUzNjVhNjkzYzgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "Wac4waEeKoiqkVC024P3M6F9T4qyJg6iXMPgtQSFewjUEPqmcqYQfL5j5Ek/bNr4D93V5qaHBd1aLkkQ/Ojn985nS0JJ7vPq+RI6UPVtYyOAJarFlfL/KMcUNVu/9BjVYjv3cupnKn4iiWWA55kA8Sze0WXa7kHFHZH4IF7NtruWxeOQIWeSCECh02CXZonuT8faB+/Scggu3UbjLhBsw4IGaUcm0kLGDR3lfRkytH8ZzLNCPhruH+F1M2tz72cUBr4TzxkgQPSIIZcZC32rp02d5kx40XHbDs184mhwfbd4qrr/lHtan94+GFUhIdYLTcUOnZ3dx1cMVHiy4KRvzVoqsg8fnm1UtWwhFW4AFlJB6i1TtC1CyDxwzqdTNkjO9Z04gOViBX762ZESYiMC4Ey6y2eTHkOF0eHDUKF4AzRd7Fk97hweDmtW47DF62l6YNCJP2dUfKxq+VoEc6fkDEkpORgWIM7JB8nComqqBLBeUc6DaikPS2Qrtb5JHhTLqr4l5EoByS5AvfxMm8OcthAqAUrq2u5/CAJrRjB+WQFHSqUPqB9+glLsn9DxLZ71OKuUcqMfdXbsKdJAI/6HWVWUKOgF9hIe3sTZQ4emI+HdrpD1dIcyXnUqdbpAtwfOIf100OT07lPzqU8rN8anIlbLDk52KMV3UUJOe+RGcIk=");

            versus.setContact(true);
            versus.setAction((target, action) -> new BedVersusMenu(target).handle());
        }

        // Shop NPC
        Location shopLoc = getLocation("npc_shop");
        if (shopLoc != null) {
            handleCommandNpc("shop", shopLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc3MDMxMzM4ODA0MCwKICAicHJvZmlsZUlkIiA6ICI0MDU4NDhjMmJjNTE0ZDhkOThkOTJkMGIwYzhiZDQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMaWFtX1NhZ2UiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk0NjZmZDc4ODg4NjY4NmRmNmQ0YWE2ODY2NjFmMTZlOTEyOTUxMDJjYjlkNDkzZmE0Mjk5YWQ3N2ZhYTRkYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "sLyL8L37+NLlfK+7aC2QXxjxdtniUQtRyumjpPY5VRH3oU7vd5xStS3vJU+XJDhzNhcXMuYHQ5npZ+vgNSx90c/+7Z7Qc4QxILFPatazY0QvT1WKgaaAx+BUrP32CySPWfoSfjdFdocJn+ssMqb2vpoPP2XvYeraN1tf8LbWuxKHVjQRLtHAFvPouVvB46hazDRrTtIziJ3LkRdr/afp/JMYcc21dmBLEGhGGZqIxhGwxq4lkotMPmB8osJY1Tg3FU9q8EW1VrvSeKGoyLZkLgj869f/AliGFyNBqUw76LQpTLpqFTpInuuuFnVWAFGVPvz/FaTN9izFH/NBhSwvnP6VDjXHcbMTi7JMN8jw2kFOQGUDESxRg2rYZlkOB3uWjbtpDEBIY5gOl/LkuYH6ouf+jAllYkK+T9xBBFsT6JTWtx1TPUpgDjYJNCztzjjffib8fzmdPqHjCZT085P1o67Azruy1AcOi3tuIU4JjUEte4SoeUnjstoABWWcNBNicF5oHknKbDVBoucelS9wbX6bSzMaZobfjt05GtlicCZZB9wvdp971UgFcaQLgq/wX5sGLkOkXFuHwb4xWySrKt8VM+Zq/b/E7vv0TOA6HcZzCNGPgwfHRNORCHk04lj/ZtGuKzA4CcX/svl5C2fCsEkX6Xr2oAfywQVMjLIF5pU=",
                    Arrays.asList("§d§l10% DE DESCONTO", "§6§lLOJA", "§eClique para ver!"),
                    "loja");

            HologramServer shopHolo = BukkitCore.getManager().getHologram().getServer("shop");
            if (shopHolo != null) {
                final int[] tick = {0};
                final String text = "10% DE DESCONTO!";
                Bukkit.getScheduler().runTaskTimer(
                        Bukkit.getPluginManager().getPlugin("Lobby"),
                        new Runnable() {
                            @Override
                            public void run() {
                                if (shopHolo.getRows().isEmpty()) return;
                                int len = text.length();
                                int step = tick[0] % 37;
                                int split;
                                if (step <= 4) {
                                    split = (step % 2 == 0) ? 0 : len;
                                } else if (step <= 20) {
                                    split = step - 4;
                                } else {
                                    split = 36 - step;
                                }
                                if (split <= 0) {
                                    shopHolo.setText(0, "§d§l" + text);
                                } else if (split >= len) {
                                    shopHolo.setText(0, "§f§l" + text);
                                } else {
                                    shopHolo.setText(0, "§f§l" + text.substring(0, split) + "§d§l" + text.substring(split));
                                }
                                tick[0]++;
                            }
                        },
                        0L, 3L
                );
            }
        }
    }

    @Override
    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction((player, action) -> new BedNavigationMenu(player, arcade, null).handle());

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + arcade.getName().toUpperCase(),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    protected void handleLeaderboards(User user) {
        Player player = user.getAccount().player();
        if (player == null || !player.isOnline()) {
            return;
        }

        HologramManager manager = BukkitCore.getManager().getHologram();

        BedMember member = user.getMember(BedMember.class);
        if (member == null) {
            return;
        }

        LeaderboardHologram rankLb = null;
        LeaderboardHologram winsLb = null;
        LeaderboardHologram killLb = null;
        LeaderboardHologram bedDestLb = null;

        boolean isRanked = member.isProfile(ProfileType.RANKED);

        Location levelLoc = getLocation("leaderboard_level");
        if (levelLoc != null) {
            HologramClient existing = manager.getClient(player, "leaderboard_level");
            if (existing != null) {
                manager.removeClient(existing);
            }
            HologramClient existingRank = manager.getClient(player, "leaderboard_competitive_rank");
            if (existingRank != null) {
                manager.removeClient(existingRank);
            }

            if (isRanked) {
                rankLb = new BedRankLeaderboard(player, levelLoc);
                rankLb.handle();
            } else {
                new BedLevelLeaderboard(player, levelLoc).handle();
            }
        }

        Location rankLoc = getLocation("leaderboard_rank");
        if (rankLoc != null) {
            HologramClient existing = manager.getClient(player, "leaderboard_rank");
            if (existing != null) {
                manager.removeClient(existing);
            }
            rankLb = new BedWinstreakLeaderboard(player, rankLoc);
            rankLb.handle();
        }

        Location winsLoc = getLocation("leaderboard_wins");
        if (winsLoc != null) {
            HologramClient existing = manager.getClient(player, "leaderboard_wins");
            if (existing != null) {
                manager.removeClient(existing);
            }
            winsLb = new BedWinsLeaderboard(player, winsLoc);
            winsLb.handle();
        }

        Location killLoc = getLocation("leaderboard_kill");
        if (killLoc != null) {
            HologramClient existing = manager.getClient(player, "leaderboard_kills");
            if (existing != null) {
                manager.removeClient(existing);
            }
            killLb = new BedFinalKillLeaderboard(player, killLoc);
            killLb.handle();
        }

        Location bedDestLoc = getLocation("leaderboard_bed_destruction");
        if (bedDestLoc != null) {
            HologramClient existing = manager.getClient(player, "leaderboard_bed_destruction");
            if (existing != null) {
                manager.removeClient(existing);
            }
            bedDestLb = new BedBedDestructionLeaderboard(player, bedDestLoc);
            bedDestLb.handle();
        }

        Location selectorLoc = getLocation("leaderboard_selector");
        if (selectorLoc != null && manager.notExistsClient(player, "leaderboard_selector")) {
            java.util.List<LeaderboardHologram> listeners = new java.util.ArrayList<>();
            if (rankLb != null) listeners.add(rankLb);
            if (winsLb != null) listeners.add(winsLb);
            if (killLb != null) listeners.add(killLb);
            if (bedDestLb != null) listeners.add(bedDestLb);

            new BedModeSelectorHologram(player, selectorLoc, listeners).handle();
        } else if (selectorLoc == null) {
            Location fallback = winsLoc != null ? winsLoc.clone().add(0, 2.8, 0)
                    : (rankLoc != null ? rankLoc.clone().add(0, 2.8, 0) : null);

            if (fallback != null && manager.notExistsClient(player, "leaderboard_selector")) {
                java.util.List<LeaderboardHologram> listeners = new java.util.ArrayList<>();
                if (rankLb != null) listeners.add(rankLb);
                if (winsLb != null) listeners.add(winsLb);
                if (killLb != null) listeners.add(killLb);

                new BedModeSelectorHologram(player, fallback, listeners).handle();
            }
        }

        Location periodSelectorLoc = getLocation("leaderboard_period_selector");
        if (periodSelectorLoc != null && manager.notExistsClient(player, "leaderboard_period_selector")) {
            java.util.List<LeaderboardHologram> periodListeners = new java.util.ArrayList<>();
            if (rankLb != null) periodListeners.add(rankLb);
            if (winsLb != null) periodListeners.add(winsLb);
            if (killLb != null) periodListeners.add(killLb);
            if (bedDestLb != null) periodListeners.add(bedDestLb);

            new BedPeriodSelectorHologram(player, periodSelectorLoc, periodListeners).handle();
        }

        Location statsLocation = getLocation("npc_stats");

        if (statsLocation != null && manager.notExistsClient(player, "stats-details")) {
            HologramClient details = BukkitCore.getManager().getHologram().spawnClient(player, "stats-details",
                    statsLocation.clone().add(0, 2.3, 0));

            if (member.isProfile(ProfileType.RANKED)) {
                Elo currentElo = member.getElo();
                Elo nextElo = member.getNextElo();
                int eloXp = member.getEloXp();
                int nextMinXp = nextElo != null ? nextElo.getMinXp() : eloXp;

                details.setText(Arrays.asList(
                        "§6Rank: " + currentElo.getFullName(),
                        "§f[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, eloXp, nextMinXp, 10) + "§f] " +
                        "§7(" + Util.formatNumberWithLetter(eloXp) + "/" + Util.formatNumberWithLetter(nextMinXp) + ")",
                        "",
                        "§7Vitórias: §a" + Util.formatNumber(member.getTotalWins()),
                        "§7Kills: §a" + Util.formatNumber(member.getTotalKills()),
                        "§7Kills finais: §a" + Util.formatNumber(member.getTotalFinalKills()),
                        "",
                        "§7Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak())
                ));
            } else {
                int xp = member.getLevelXp();
                int minXp = member.getMinLevelXp();

                details.setText(Arrays.asList(
                        "§6Seu nível: " + member.getLevelId(),
                        "§f[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10) + "§f] " +
                        "§7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")",
                        "",
                        "§7Vitórias: §a" + Util.formatNumber(member.getTotalWins()),
                        "§7Kills: §a" + Util.formatNumber(member.getTotalKills()),
                        "§7Kills finais: §a" + Util.formatNumber(member.getTotalFinalKills()),
                        "",
                        "§7Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak())
                ));
            }

            details.spawnTo(player);
        }
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();
        BedMember member = user.getMember(BedMember.class);
        player.getWorld().getPlayers().forEach(worldPlayer -> {
            Account target = Core.getAccountController().of(worldPlayer.getUniqueId());
            if (target == null) return;

            StringBuilder builder = new StringBuilder();

            if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                builder.append(account.getMedal().getColoredSymbol()).append(" ");
            }

            builder.append(member.isProfile(ProfileType.RANKED) ? member.getElo().getChat() : member.getLevelId()).append(" ");

            builder.append(account.getTag().getByPrefix(target.getTagPrefix()))
                    .append(account.getNickname())
                    .append(": ")
                    .append(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(TextComponent.fromLegacyText(builder.toString()));
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(1.8).setY(0.6);
    }

    @EventHandler
    public void onProfileChange(AccountProfileChangeEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());
        if (user != null) {
            Core.getPlatform().runSync(() -> {
                handleSidebar(user);
                handleLeaderboards(user);
            }, 5L);
        }
    }

    @EventHandler
    public void onHologramUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            HologramServer hologram = BukkitCore.getManager().getHologram().getServer("Duels");

            if (hologram != null) {
                int onlinePlayers = 0;

                for (ArcadeCategory arcade : ArcadeCategory.list()) {
                    if (arcade.getServer().equals(ServerType.BEDWARS) && arcade.name().contains("VERSUS"))
                        onlinePlayers += Core.getArcadeData().getOnlinePlayers(arcade);
                }

                hologram.setText(hologram.getRows().size() - 1, "§e" + onlinePlayers + " jogando.");
            }
        }
    }
}