package com.minecraft.lobby.architect.list.eggwars;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.menu.server.arcade.mode.eggwars.EggWarsMenu;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.BedCollectibleController;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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

public class EggWarsArchitect extends Architect {

    public EggWarsArchitect() {
        super(ServerType.HUB_EGGWARS);
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

        EggMember member = Core.getEggWarsData().of(id, true);

        if (member == null)
            member = Core.getEggWarsData().save(new EggMember(id, player.getName()));

        Core.getMemberController().save(member);
        Core.getEggWarsData().cancelExpiration(id);

        User user = (User) User.of(player.getUniqueId());
        super.join(player);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getEggWarsData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        EggMember member = user.getMember(EggMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lEGG WARS");
        sidebar.blankRow();

        int xp = member.getLevelXp();
        int minXp = member.getMinLevelXp();

        sidebar.addRow("rank", "Seu nível: " + member.getLevelId());

        sidebar.addRow("progress", " §8[" + Util.createProgressBar(
                ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10, false) + "§8]"
                + " §7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");

        sidebar.blankRow();

        sidebar.addRow("solo", "§eSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.EGGWARS_SOLO)));
        sidebar.addRow("solo_winstreak", " Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.EGGWARS_SOLO)));

        sidebar.addRow("duo", "§eDuplas:");
        sidebar.addRow("duo_wins", " §fVitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.EGGWARS_DUO)));
        sidebar.addRow("duo_winstreak", " §fWinstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.EGGWARS_DUO)));

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

        PlayerInventory inv = player.getInventory();

        inv.setItem(2, Item.of(Material.DRAGON_EGG, "§aMenu do " + ServerType.EGGWARS.getName())
                .interact(event -> new EggWarsMenu(event.getPlayer()).handle()));
    }

    @Override
    public void handleEntities() {
        super.handleEntities();

        Core.getLogger().info("[EggWarsArchitect] Carregando NPCs no mundo '" + getWorld().getName() + "' com " + getLocations().size() + " locations configuradas.");
        Location statsLoc = getLocation("npc_stats");
        Location soloLoc = getLocation("npc_solo");
        Location duoLoc = getLocation("npc_duo");
        Location trioLoc = getLocation("npc_trio");
        Location quartetLoc = getLocation("npc_quartet");
        Location versusLoc = getLocation("npc_versus");

        Core.getLogger().info("[EggWarsArchitect] Locations encontradas: stats=" + (statsLoc != null) +
                ", solo=" + (soloLoc != null) + ", duo=" + (duoLoc != null) +
                ", trio=" + (trioLoc != null) + ", quartet=" + (quartetLoc != null) +
                ", versus=" + (versusLoc != null));

        for (SignedLocation signed : getLocationsByName("dragon_egg")) {
            Location egg = signed.getSynthetic().of(getWorld());

            if (egg != null)
                buildHeadRotation(egg);
        }

        if (statsLoc != null) {
            handleStatsNpc(statsLoc);
        }

        if (soloLoc != null) {
            handleArcadeNpc(ArcadeCategory.EGGWARS_SOLO, soloLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxOTI5NDc0NjQ5NiwKICAicHJvZmlsZUlkIiA6ICJjMGYzYjI3YTUwMDE0YzVhYjIxZDc5ZGRlMTAxZGZlMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDEzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmNDFlNDIwMmI4YTFhMTMzZjBiYjVlZWFhM2IyZTA2NmJiZWYzOTZlNTkxMTVkODg2OTliZTE5NTlkY2M3MTciCiAgICB9CiAgfQp9",
                    "RD0qdkGjt0Dl7VD2zq9gM0BuZPcRxrXyQG8YEXxyZVdw6pbDt/v51PeONCG42RutEGjCWgsu31UWnqjEDMXKT8dAfELUtZtJmEkYosm73037oLFY8jMvJIwrw+sOIOVIQXg+jzyo6/ySAxj217yLDA7eXfWYIPWYD8Nnd/CfojlcsgrKtYytSoy5py4g+AjX5P+v15kEHP/NxGxmULo7Fa4EFUInoNh0B0ZEi7fDa66LnnLekaeCDdD+PqWihnosq4r88/GJSPgRGJbfLTEwN/WHZqGMVoOgKEuSu7x3emOQFU4/Hw26KdOtHdFXAFNNwVeDjD8JU9+Sd/bpD74K9zjDNJaPSgiGld3San0vQHEUEstF8CffpUCNwR5EdX3fRAaNmPPG2OaNre0IylWrH6FeivZ6iDQjnKV2zNJh9+0GPtmwiwjwO28Sn4jhAbmo90oWiZbxuXbem/x4UEpKCrgeYo+gNkwW1+fYZszNwca9IPb+YSxR/K2WyzYLaA5NYcevAwuuPhbVFRBa0fVySN1KAav4ReByBQMV+olgDZNJWqBxfK9h/W1WFASp0wwLDD5GFWqeEoUoPfFMXOrRDjET6uLXl7tsi7iEcdWtrwxAh4b0D3siYQ6cNaoYNuSMa1mpsDBaPYGbKwXJNl+ViTSqJQKPcKvCDMhCuiVe9lM=");
        }

        if (duoLoc != null) {
            handleArcadeNpc(ArcadeCategory.EGGWARS_DUO, duoLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcxMzE4NjgzMTUxOCwKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFkOWVhOTRjOTU3ZWMyOGU2YzVhM2U1YzlkNGQ3NmEzMzU0ZjQ2MDE5ZTZhOGUwNTViZTYyMmJlYmMwNjY5ZDQiCiAgICB9CiAgfQp9",
                    "maG14gs+bGf28lxQ4ZbQxmcf6xmhhCWqL292YfNyFWHyDvxXGLBCApcGIFH/vdhe1Jdfj2M2Yciz27nLn82wWvZz+Cn9ETfKeNHxmrDBkYbuRTOU/7XOvDtIfq5MQTSXeH69ZLpr2N72GxCLfApfEEi87gkoJibSLxDMZB7rMcso9v+liAV/Cw0SCF8Mwx2KsQxkg84uRpvANr+eO5vFp+BgENT41NPCDTYw74iQbQB4n6fU7I3Oqkiuo9BiLsGr+ZBmgwyCtfjH/E1TgQma/JMobBsBMqm4lh0oYcS0u95ffdBQphd9FKr2hStfpSwBcHQOKNYpIS6ZLQP6/be6+jmHjH5CUGUEYoVXNWcBFCzjFsPRbyHuZJSnCzxHOgr7MLJXo14YGOFsapUJ4n4KarzhccWpZ1InkG8ABc3ClHKmSonMEftH4gck0FH/AzEUO0OCAEdaYc2xyHGuTfijdL/B2g4kLVlKuafdlF31f6USTQHK1SVSX8RIqyxGyyBXr81Y45Zc7v1dk/FsiyrTat8pzF9iLwMpkxHN1oJpwU/9o8acbth92bfRU6QdHL+Ad2/AsONbHWSV4GKygSV/INoFqvH6CtjtS2Z3icGU1TaK/jT5Dcv8Oq//0QXGDXPIFhAbcQIKRERftH4Ai7En/pvvHVqw02Gw9DvnvWRGWz0=");
        }

        if (trioLoc != null) {
            handleArcadeNpc(ArcadeCategory.EGGWARS_TRIO, trioLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyNTQ4MjEyOTE2NywKICAicHJvZmlsZUlkIiA6ICIzZDU1OGQ3Y2NmZjk0ODdkYWE1MzhkMjM4NGE3OWFkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcnlwdGljTG9zZXIxMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNjJiMjM5MjA3YjcxN2UxZDZkMzhhMzRkNmM2Y2EzYzg4YzJkOWEyZmE0NTQ1YjM0MWNiYzNlMjBjMjBhM2E0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "f0VAeuolcsQrNQEmS/l/HkAukjLWw7sRfJaJH317Bt4iEvCt6cf9LBfbqm/eyHtVH6yOxB+8nC9xcZDtBp2WTpmDpOQegEPYWYbn7RcfKchuJ2JErVG8Di3K4NQTr3wSCoSDOuwQCjJ0UCxVsocrOa4y4dfWudS8lj4Qy+aexkwzdAJg1Che51btv3diZ8G9ILx9ARK2E6H0enoX2kelVj8imYmBOSotGU1E+CVcbp+Sfm0TTRGvl44VmVvGBLP4vjVGd+exMvqIGZjC2t+8T7GNlAy5lfWXwY/mFvs6q5r0yIIj4rCcNOxpU+lg3k+2occnvd1Qfg3eDF3PYVBuSAP/n1ncmlTnPzEsWim4hsWUHNeETL46sfw13tlMTNEHnHY0StxS9HJS373omBNj8tT7meH65hhykpJdwWylooBAeaHUAOQxmfgTt68hX/7+fpdOu/vUaOgRa6Cq9kTg20n2ow065+TiOiaBnZ5J8B2evTLii5kumpWwKnXRpJP455m2SUNppWH1AAZ63ZtNdg6oUe5aLmO5W+Ps6cN4Kn2PMg5wVd7wqZ0jlxw9/qTPemrkxSFguwA8h6ZyzLQsIk5teBp+GNeSJBFa2pTCQJU8hK1Av8ctZfLsorj59JD620rTgdI7ST4Dcub4xjict4MICirCjR1oyhbtVmlPg/0=");
        }

        if (quartetLoc != null) {
            handleArcadeNpc(ArcadeCategory.EGGWARS_QUARTET, quartetLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxMTMyNzMxMDE3NCwKICAicHJvZmlsZUlkIiA6ICJmNWQwYjFhZTQxNmU0YTE5ODEyMTRmZGQzMWU3MzA1YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRjaFRoZVdhdmUxMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZjFmZTQxY2FkODVmYWU3MzliNGZkMDk4NzQ4NTAxMWQ3NTA3MzFjZDViNjU2NmE0ZjVhOWY0YTliMjU2ZTlhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "smmVV+Zx+XsHATxU4MIiMUYnhAPE3qMzm9+VPYWkgzKeZyPYAVNPCxs5GJep7AVZw9eZvcPhqF6nfL/Xrbf8NP20qju7guEPYi3JfZBU/QKjbKgIoPjMgJSUlEvR5bhYuTSz2pU2ICZA3GvbAf/pb8dO5wR1tgi3B07BYQqLOJDJZwd2xjElHmbDLCUrx6fWzo19AS2Ku5Uglcu6BS07uGwo5z3x4DsUQXn5uxzqMU0hZmXi/nQFjfs3BQz/Ge5TdhnI/SwhefME1iQkVe3aUDTmCMBvdkqS7tbNBemcvH3PgkAmQg/JzTP/5Z4pkGz9EPZsOwtu31SXhYtT5V+OWn+vlwhlCdWEcQFDd+jG37ZS8Z9a/QahfDLmoA2VC1nxl/aDXDEosbuV9uNnOJCsdjHm+rgGdpUkPBdU6DPlQLYri60B1fAYq4i9HQO/EyB8hPk5Vr2Og/PsBWiW3YiJhmrws6rnFCdvZPoefMaypW4N9qhS7oSjH3A/FGtRN19QyHC6GDdUnb15YZGxzzW0ZkgdOemdFF6DYeCpmVaK7FJQg64E/8zTeI5h3jqqsZsBCs45v8X9wFL94oVauAnXWb1FJ7KTAzlLke9C91W2+PkcWiXWVXmQ/eSCpJapksZwJiNucraLbat8GqgENGYeX1e/zK1YZ5hrICSLBGGZfzE=");
        }

        if (versusLoc != null) {
            NpcServer versus = handleNpc("EGGWARS_VERSUS", versusLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYyNjcwNDg1ODc2NSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFjNGE4MjlhODg0M2I0ZTgzOWIwMGY2ZmE3OTlkMWFiYjYzN2NhMjU0Y2MwNTRhYTcyNjUwZTUzNjVhNjkzYzgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "Wac4waEeKoiqkVC024P3M6F9T4qyJg6iXMPgtQSFewjUEPqmcqYQfL5j5Ek/bNr4D93V5qaHBd1aLkkQ/Ojn985nS0JJ7vPq+RI6UPVtYyOAJarFlfL/KMcUNVu/9BjVYjv3cupnKn4iiWWA55kA8Sze0WXa7kHFHZH4IF7NtruWxeOQIWeSCECh02CXZonuT8faB+/Scggu3UbjLhBsw4IGaUcm0kLGDR3lfRkytH8ZzLNCPhruH+F1M2tz72cUBr4TzxkgQPSIIZcZC32rp02d5kx40XHbDs184mhwfbd4qrr/lHtan94+GFUhIdYLTcUOnZ3dx1cMVHiy4KRvzVoqsg8fnm1UtWwhFW4AFlJB6i1TtC1CyDxwzqdTNkjO9Z04gOViBX762ZESYiMC4Ey6y2eTHkOF0eHDUKF4AzRd7Fk97hweDmtW47DF62l6YNCJP2dUfKxq+VoEc6fkDEkpORgWIM7JB8nComqqBLBeUc6DaikPS2Qrtb5JHhTLqr4l5EoByS5AvfxMm8OcthAqAUrq2u5/CAJrRjB+WQFHSqUPqB9+glLsn9DxLZ71OKuUcqMfdXbsKdJAI/6HWVWUKOgF9hIe3sTZQ4emI+HdrpD1dIcyXnUqdbpAtwfOIf100OT07lPzqU8rN8anIlbLDk52KMV3UUJOe+RGcIk=");

            versus.setContact(true);
            versus.setAction((target, action) -> target.sendMessage("§cew- Menu em desenvolvimento."));
        }

        handleTopNpc();
    }

    @Override
    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction((target, action) -> target.sendMessage("§cew- Menu em desenvolvimento."));

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + arcade.getName().toUpperCase(),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    protected void handleTopNpc() {
        List<EggMember> topList = Core.getEggWarsData().ranking("context.eloXp", 3)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        int index = 1;
        for (EggMember member : topList) {
            if (member == null) continue;

            Account account = member.getAccount();

            if (account == null) continue;

            Location location = getLocation("npc_top_" + index);

            if (location == null) continue;

            if (index == 1 && BukkitCore.getManager().getHologram().notExistsServer("top_rank")) {
                HologramServer server = BukkitCore.getManager().getHologram().spawnServer("top_rank",
                        location.clone().add(0, 1.5, 0));

                server.setText(Collections.singletonList("§6§lTOP RANK COMPETITIVO"));
            }

            boolean notExists = BukkitCore.getManager().getNpc().notExistsServer("npc_top_" + index);

            NpcServer npc = notExists
                    ? BukkitCore.getManager().getNpc().spawnServer(location, account.getSkin().getValue(), account.getSkin().getSignature())
                    : BukkitCore.getManager().getNpc().getServer("npc_top_" + index);

            if (notExists) {
                npc.setTag("npc_top_" + index);

                npc.setContact(true);

                npc.display();
            } else
                npc.updateTexture(new Property("textures", account.getSkin().getValue(), account.getSkin().getSignature()));
            index++;
        }
    }

    public void buildHeadRotation(Location location) {
        location = location.clone().add(0, 1.5, 0);

        ItemStack helmet = new ItemStack(Material.DRAGON_EGG);

        location = location.clone().add(0.5, 0, 0.5);

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("dragon_egg-" + StringUtil.generateExclusiveCode(5), location);

        hologram.setText(Collections.singletonList(""));

        hologram.addAnimatedRow(helmet);
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        EggMember member = user.getMember(EggMember.class);

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
    public void onHologramUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            if (event.getTicks() % 6000 == 0)
                handleTopNpc();
        }

        if (event.isType(UpdateType.SECOND)) {
            HologramServer hologram = BukkitCore.getManager().getHologram().getServer("EGGWARS_VERSUS");

            if (hologram != null) {
                int onlinePlayers = 0;

                for (ArcadeCategory arcade : ArcadeCategory.list()) {
                    if (arcade.getServer().equals(ServerType.EGGWARS) && arcade.name().contains("VERSUS"))
                        onlinePlayers += Core.getArcadeData().getOnlinePlayers(arcade);
                }

                hologram.setText(hologram.getRows().size() - 1, "§e" + onlinePlayers + " jogando.");
            }
        }
    }
}
