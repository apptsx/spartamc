package com.minecraft.lobby.architect.list.skywars;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.skywars.SkyMember;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.architect.list.skywars.leaderboard.SkyKillsLeaderboard;
import com.minecraft.lobby.architect.list.skywars.leaderboard.SkyWinsLeaderboard;
import com.minecraft.lobby.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyWarsArchitect extends Architect {

    public SkyWarsArchitect() {
        super(ServerType.HUB_SKYWARS);
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void join(Player player) {
        UUID id = player.getUniqueId();

        SkyMember member = Core.getSkyWarsData().of(id, true);

        if (member == null)
            member = Core.getSkyWarsData().save(new SkyMember(id, player.getName()));

        Core.getMemberController().save(member);
        Core.getSkyWarsData().cancelExpiration(id);

        User user = (User) User.of(player.getUniqueId());
        super.join(player);

        CompletableFuture.runAsync(() -> handleLeaderboards(user));
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getSkyWarsData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        SkyMember member = user.getMember(SkyMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lSKY WARS");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + getId());
        sidebar.blankRow();

        int xp = member.getLevelXp();
        int minXp = member.getMinLevelXp();

        sidebar.addRow("rank", "Seu nível: " + member.getLevelId());

        sidebar.addRow("progress", " §8[" + Util.createProgressBar(
                ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10, false) + "§8]"
                + " §7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");

        sidebar.blankRow();

        sidebar.addRow("solo", "§bSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.SKYWARS_SOLO)));
        sidebar.addRow("solo_kills", " Kills: §a" + Util.formatNumber(member.getTotalKills()));

        sidebar.addRow("duo", "§bDuplas:");
        sidebar.addRow("duo_wins", " §fVitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.SKYWARS_DUO)));

        sidebar.blankRow();
        sidebar.addRow("players", "Players: §a" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleHotbar(Player player) {
        super.handleHotbar(player);

        PlayerInventory inv = player.getInventory();

//        inv.setItem(2, Item.of(Material.BOW, "§aMenu do " + ServerType.SKYWARS.getName()).interact(event -> new SkyWarsMenu(event.getPlayer()).handle()));
    }

    @Override
    public void handleEntities() {
        super.handleEntities();
        
        Core.getLogger().info("[SkyWarsArchitect] Carregando NPCs no mundo '" + getWorld().getName() + "' com " + getLocations().size() + " locations configuradas.");
        
        // Buscar locations individualmente para evitar problemas com índices
        Location statsLoc = getLocation("npc_stats");
        Location soloLoc = getLocation("npc_solo");
        Location duoLoc = getLocation("npc_duo");
        Location trioLoc = getLocation("npc_trio");
        Location quartetLoc = getLocation("npc_quartet");
        Location versusLoc = getLocation("npc_versus");
        
        // Log das locations encontradas
        Core.getLogger().info("[SkyWarsArchitect] Locations encontradas: stats=" + (statsLoc != null) +
                ", solo=" + (soloLoc != null) + ", duo=" + (duoLoc != null) + 
                ", trio=" + (trioLoc != null) + ", quartet=" + (quartetLoc != null) + 
                ", versus=" + (versusLoc != null));

        if (statsLoc != null) {
            handleStatsNpc(statsLoc);
        }

        if (soloLoc != null) {
            handleArcadeNpc(ArcadeCategory.SKYWARS_SOLO, soloLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTczNzQ4MDY4NTgzMiwKICAicHJvZmlsZUlkIiA6ICJjNDIzYjQwMWZiOGU0ODc3YjMzMmVmMjhiZDdlZGZmZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFjdGlvbkJyaW5lWVQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjczYjY0ODNkNjRmYjA2N2U2NzY1YjQ1MmM5N2MxMmM1YTQ5MjU3M2U1ZDhkOGExYjdjNzU4MTNkMzQ5MGJhOSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "eDqCVD+Sb3e4tkPvRk65udCjFvgOSxXu8T++CocYQUuAMqRy0cz8mj7KdyoJJAsefmagUpiIRjwdbNrdBL74mb3dQff0M5FdmeqR2DXP6KIP2XLUQl2rXg5ugdzyIEx75eAV22nzehgiDmrYnCWTAtNFuvrIkNs9p8/WhPLzP1wmWbQvDB0zwCXD9BITO+bVRmKFqkXY0BrBDC/CsTmI1R5MF4ij+xB0HMe+EuEWgmZjnnvxRoOX91B7YzciOfG66gh/tLWU9Rz7sbn3zt8JjYzEBXGVj/X9aWXNguzFWgpWAJkX6YCtjG1hsLXK0Pc0maqS7xxTuk/tKfuAh4Fj45gQcdRz6JY/L4MZ3aTSsqODNVp2nWNxyW4MK6P/mHPfC1AZRh6E3u1Wr49LAOBWzIo+yvouj0CXb5s3tOpyu0xrR8rCr+V3io22Zy3BLlVA3YL8soqTox05GfTRi5tvUpfyia9PUC0K0bAvd43ReDAoKaV8CU1tvQjSL4q3vPFgb8emT34KtbuiOFj+XeBTCJkF9PkFd591Vtjq96SYN1MZ9nPQbV1SLj0lhm4Ahr8nViOWCvz28DTaLX+lQXOA1ya3VLJFMG0Es2p1GTASUlEQW7gBpV5k4I7yU1OLZpHmMtpQ5NKIg0T5J0jPOTK3ufULHhLHi75fI+RCCCJrwNo=");
        }

//        handleArcadeNpc(ArcadeCategory.SKYWARS_DUO, list.get(2),
//                "ewogICJ0aW1lc3RhbXAiIDogMTcxMzE4NjgzMTUxOCwKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFkOWVhOTRjOTU3ZWMyOGU2YzVhM2U1YzlkNGQ3NmEzMzU0ZjQ2MDE5ZTZhOGUwNTViZTYyMmJlYmMwNjY5ZDQiCiAgICB9CiAgfQp9",
//                "maG14gs+bGf28lxQ4ZbQxmcf6xmhhCWqL292YfNyFWHyDvxXGLBCApcGIFH/vdhe1Jdfj2M2Yciz27nLn82wWvZz+Cn9ETfKeNHxmrDBkYbuRTOU/7XOvDtIfq5MQTSXeH69ZLpr2N72GxCLfApfEEi87gkoJibSLxDMZB7rMcso9v+liAV/Cw0SCF8Mwx2KsQxkg84uRpvANr+eO5vFp+BgENT41NPCDTYw74iQbQB4n6fU7I3Oqkiuo9BiLsGr+ZBmgwyCtfjH/E1TgQma/JMobBsBMqm4lh0oYcS0u95ffdBQphd9FKr2hStfpSwBcHQOKNYpIS6ZLQP6/be6+jmHjH5CUGUEYoVXNWcBFCzjFsPRbyHuZJSnCzxHOgr7MLJXo14YGOFsapUJ4n4KarzhccWpZ1InkG8ABc3ClHKmSonMEftH4gck0FH/AzEUO0OCAEdaYc2xyHGuTfijdL/B2g4kLVlKuafdlF31f6USTQHK1SVSX8RIqyxGyyBXr81Y45Zc7v1dk/FsiyrTat8pzF9iLwMpkxHN1oJpwU/9o8acbth92bfRU6QdHL+Ad2/AsONbHWSV4GKygSV/INoFqvH6CtjtS2Z3icGU1TaK/jT5Dcv8Oq//0QXGDXPIFhAbcQIKRERftH4Ai7En/pvvHVqw02Gw9DvnvWRGWz0=");
//
//        handleArcadeNpc(ArcadeCategory.SKYWARS_TRIO, list.get(3),
//                "ewogICJ0aW1lc3RhbXAiIDogMTcyNTQ4MjEyOTE2NywKICAicHJvZmlsZUlkIiA6ICIzZDU1OGQ3Y2NmZjk0ODdkYWE1MzhkMjM4NGE3OWFkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcnlwdGljTG9zZXIxMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNjJiMjM5MjA3YjcxN2UxZDZkMzhhMzRkNmM2Y2EzYzg4YzJkOWEyZmE0NTQ1YjM0MWNiYzNlMjBjMjBhM2E0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
//                "f0VAeuolcsQrNQEmS/l/HkAukjLWw7sRfJaJH317Bt4iEvCt6cf9LBfbqm/eyHtVH6yOxB+8nC9xcZDtBp2WTpmDpOQegEPYWYbn7RcfKchuJ2JErVG8Di3K4NQTr3wSCoSDOuwQCjJ0UCxVsocrOa4y4dfWudS8lj4Qy+aexkwzdAJg1Che51btv3diZ8G9ILx9ARK2E6H0enoX2kelVj8imYmBOSotGU1E+CVcbp+Sfm0TTRGvl44VmVvGBLP4vjVGd+exMvqIGZjC2t+8T7GNlAy5lfWXwY/mFvs6q5r0yIIj4rCcNOxpU+lg3k+2occnvd1Qfg3eDF3PYVBuSAP/n1ncmlTnPzEsWim4hsWUHNeETL46sfw13tlMTNEHnHY0StxS9HJS373omBNj8tT7meH65hhykpJdwWylooBAeaHUAOQxmfgTt68hX/7+fpdOu/vUaOgRa6Cq9kTg20n2ow065+TiOiaBnZ5J8B2evTLii5kumpWwKnXRpJP455m2SUNppWH1AAZ63ZtNdg6oUe5aLmO5W+Ps6cN4Kn2PMg5wVd7wqZ0jlxw9/qTPemrkxSFguwA8h6ZyzLQsIk5teBp+GNeSJBFa2pTCQJU8hK1Av8ctZfLsorj59JD620rTgdI7ST4Dcub4xjict4MICirCjR1oyhbtVmlPg/0=");
//
//        handleArcadeNpc(ArcadeCategory.SKYWARS_QUARTET, list.get(4),
//                "ewogICJ0aW1lc3RhbXAiIDogMTYxMTMyNzMxMDE3NCwKICAicHJvZmlsZUlkIiA6ICJmNWQwYjFhZTQxNmU0YTE5ODEyMTRmZGQzMWU3MzA1YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRjaFRoZVdhdmUxMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZjFmZTQxY2FkODVmYWU3MzliNGZkMDk4NzQ4NTAxMWQ3NTA3MzFjZDViNjU2NmE0ZjVhOWY0YTliMjU2ZTlhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
//                "smmVV+Zx+XsHATxU4MIiMUYnhAPE3qMzm9+VPYWkgzKeZyPYAVNPCxs5GJep7AVZw9eZvcPhqF6nfL/Xrbf8NP20qju7guEPYi3JfZBU/QKjbKgIoPjMgJSUlEvR5bhYuTSz2pU2ICZA3GvbAf/pb8dO5wR1tgi3B07BYQqLOJDJZwd2xjElHmbDLCUrx6fWzo19AS2Ku5Uglcu6BS07uGwo5z3x4DsUQXn5uxzqMU0hZmXi/nQFjfs3BQz/Ge5TdhnI/SwhefME1iQkVe3aUDTmCMBvdkqS7tbNBemcvH3PgkAmQg/JzTP/5Z4pkGz9EPZsOwtu31SXhYtT5V+OWn+vlwhlCdWEcQFDd+jG37ZS8Z9a/QahfDLmoA2VC1nxl/aDXDEosbuV9uNnOJCsdjHm+rgGdpUkPBdU6DPlQLYri60B1fAYq4i9HQO/EyB8hPk5Vr2Og/PsBWiW3YiJhmrws6rnFCdvZPoefMaypW4N9qhS7oSjH3A/FGtRN19QyHC6GDdUnb15YZGxzzW0ZkgdOemdFF6DYeCpmVaK7FJQg64E/8zTeI5h3jqqsZsBCs45v8X9wFL94oVauAnXWb1FJ7KTAzlLke9C91W2+PkcWiXWVXmQ/eSCpJapksZwJiNucraLbat8GqgENGYeX1e/zK1YZ5hrICSLBGGZfzE=");
//
//        NpcServer versus = handleNpc("VERSUS", list.get(5),
//                "ewogICJ0aW1lc3RhbXAiIDogMTYyNjcwNDg1ODc2NSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFjNGE4MjlhODg0M2I0ZTgzOWIwMGY2ZmE3OTlkMWFiYjYzN2NhMjU0Y2MwNTRhYTcyNjUwZTUzNjVhNjkzYzgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
//                "Wac4waEeKoiqkVC024P3M6F9T4qyJg6iXMPgtQSFewjUEPqmcqYQfL5j5Ek/bNr4D93V5qaHBd1aLkkQ/Ojn985nS0JJ7vPq+RI6UPVtYyOAJarFlfL/KMcUNVu/9BjVYjv3cupnKn4iiWWA55kA8Sze0WXa7kHFHZH4IF7NtruWxeOQIWeSCECh02CXZonuT8faB+/Scggu3UbjLhBsw4IGaUcm0kLGDR3lfRkytH8ZzLNCPhruH+F1M2tz72cUBr4TzxkgQPSIIZcZC32rp02d5kx40XHbDs184mhwfbd4qrr/lHtan94+GFUhIdYLTcUOnZ3dx1cMVHiy4KRvzVoqsg8fnm1UtWwhFW4AFlJB6i1TtC1CyDxwzqdTNkjO9Z04gOViBX762ZESYiMC4Ey6y2eTHkOF0eHDUKF4AzRd7Fk97hweDmtW47DF62l6YNCJP2dUfKxq+VoEc6fkDEkpORgWIM7JB8nComqqBLBeUc6DaikPS2Qrtb5JHhTLqr4l5EoByS5AvfxMm8OcthAqAUrq2u5/CAJrRjB+WQFHSqUPqB9+glLsn9DxLZ71OKuUcqMfdXbsKdJAI/6HWVWUKOgF9hIe3sTZQ4emI+HdrpD1dIcyXnUqdbpAtwfOIf100OT07lPzqU8rN8anIlbLDk52KMV3UUJOe+RGcIk=");
//
//        versus.setContact(true);
//        versus.setAction((target, action) -> {
//            // TODO: Criar menu versus
//            target.sendMessage("§cMenu Versus em desenvolvimento!");
//        });

//        handleTopNpc();
    }

    @Override
    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
//        npc.setAction((player, action) -> new SkyNavigationMenu(player, arcade, null).handle());

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§b§l" + arcade.getName().toUpperCase(),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    protected void handleLeaderboards(User user) {
        Player player = user.getAccount().player();
        if (player == null || !player.isOnline()) return;

        HologramManager manager = BukkitCore.getManager().getHologram();

        SkyMember member = user.getMember(SkyMember.class);

        Location winsLoc = getLocation("leaderboard_wins");
        if (winsLoc != null && manager.notExistsClient(player, "leaderboard_wins")) {
            new SkyWinsLeaderboard(player, winsLoc).handle();
        }

        Location killsLoc = getLocation("leaderboard_kills");
        if (killsLoc != null && manager.notExistsClient(player, "leaderboard_kills")) {
            new SkyKillsLeaderboard(player, killsLoc).handle();
        }

        Location statsLocation = getLocation("npc_stats");

        if (statsLocation != null && manager.notExistsClient(player, "stats-details")) {
            HologramClient details = BukkitCore.getManager().getHologram().spawnClient(player, "stats-details",
                    statsLocation.clone().add(0, 2.3, 0));

            int xp = member.getLevelXp();
            int minXp = member.getMinLevelXp();

            details.setText(Arrays.asList(
                    "Seu nível: " + member.getLevelId(),
                    "§f[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10) + "§f] " +
                            "§7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")",
                    "",
                    "Vitórias: §a" + Util.formatNumber(member.getTotalWins()),
                    "Kills: §a" + Util.formatNumber(member.getTotalKills()),
                    "",
                    "Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak())
            ));

            details.spawnTo(player);
        }
    }

//    protected void handleTopNpc() {
//        List<SkyMember> topList = SkyMinigames.getSkyWarsData().ranking("context.eloXp", 3)
//                .stream()
//                .limit(3)
//                .toList();
//
//        int index = 1;
//        for (SkyMember member : topList) {
//            if (member == null) continue;
//
//            Account account = member.getAccount();
//
//            if (account == null) continue;
//
//            Location location = getLocation("npc_top_" + index);
//
//            if (location == null) continue;
//
//            if (index == 1 && BukkitCore.getManager().getHologram().notExistsServer("top_rank")) {
//                HologramServer server = BukkitCore.getManager().getHologram().spawnServer("top_rank",
//                        location.clone().add(0, 1.5, 0));
//
//                server.setText(Collections.singletonList("§b§lTOP RANK COMPETITIVO"));
//            }
//
//            boolean notExists = BukkitCore.getManager().getNpc().notExistsServer("npc_top_" + index);
//
//            NpcServer npc = notExists
//                    ? BukkitCore.getManager().getNpc().spawnServer(location, account.getSkin().getValue(), account.getSkin().getSignature())
//                    : BukkitCore.getManager().getNpc().getServer("npc_top_" + index);
//
//            if (notExists) {
//                npc.setTag("npc_top_" + index);
//
//                npc.setContact(true);
//
//                npc.display();
//            } else
//                npc.updateTexture(new Property("textures", account.getSkin().getValue(), account.getSkin().getSignature()));
//
//            HologramServer hologram = BukkitCore.getManager().getHologram().notExistsServer("npc_top_" + index)
//                    ? BukkitCore.getManager().getHologram().spawnServer("npc_top_" + index, location)
//                    : BukkitCore.getManager().getHologram().getServer("npc_top_" + index);
//
//            hologram.setText(Arrays.asList(
//                    (index == 1 ? "§a" : index == 2 ? "§e" : "§c") + index + "º",
//                    "§fRank: " + member.getElo().getFullName() + " §7(XP: " + Util.formatNumberWithLetter(member.getEloXp()) + ")",
//                    account.getRank().getColor() + account.getName()
//            ));
//
//            index++;
//        }
//    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        SkyMember member = user.getMember(SkyMember.class);

        if (member == null) {
            player.sendMessage("§cNão foi possível recolher os seus dados. Contate um administrador!");
            return;
        }

        player.getWorld().getPlayers().forEach(worldPlayer -> {
            Account target = Core.getAccountController().of(worldPlayer.getUniqueId());
            if (target == null) return;
            
            StringBuilder builder = new StringBuilder();
            
            builder.append(member.getLevelId());
            
            if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                builder.append(account.getMedal().getColoredSymbol()).append(" ");
            }
            
            builder.append(account.getTag().getByPrefix(target.getTagPrefix()))
                   .append(account.getNickname())
                   .append(" §7» ")
                   .append(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(TextComponent.fromLegacyText(builder.toString()));
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(1.8).setY(0.6);
    }

    @EventHandler
    public void onHologramUpdate(AsyncUpdateEvent event) {
//        if (event.isType(UpdateType.TICK)) {
//            if (event.getTicks() % 6000 == 0)
//                handleTopNpc();
//        }

        if (event.isType(UpdateType.SECOND)) {
            HologramServer hologram = BukkitCore.getManager().getHologram().getServer("VERSUS");

            if (hologram != null) {
                int onlinePlayers = 0;

                for (ArcadeCategory arcade : ArcadeCategory.list()) {
                    if (arcade.getServer().equals(ServerType.SKYWARS) && arcade.name().contains("VERSUS"))
                        onlinePlayers += Core.getArcadeData().getOnlinePlayers(arcade);
                }

                hologram.setText(hologram.getRows().size() - 1, "§e" + onlinePlayers + " jogando.");
            }
        }
    }
}

