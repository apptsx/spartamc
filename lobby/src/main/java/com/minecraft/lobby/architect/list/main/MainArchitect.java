package com.minecraft.lobby.architect.list.main;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.Scroller;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.architect.list.main.leaderboard.TopPunitionsLeaderboard;
import com.minecraft.lobby.architect.list.main.leaderboard.WoolsPlacedLeaderboard;
import com.minecraft.lobby.menu.shop.ShopMenu;
import com.minecraft.lobby.menu.treino.TreinoPonteMenu;
import com.minecraft.lobby.user.User;
import com.minecraft.lobby.util.VanishParticlesTask;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MainArchitect extends Architect {

    public MainArchitect() {
        super(ServerType.HUB);
    }

    @Override
    public void load() {
        super.load();
        
        // Carregar cosméticos
        Core.getCollectibleController().handle(com.minecraft.lobby.Lobby.getInstance(), "com.minecraft.core.api.collectible");
        
        // Carregar leaderboards
        CompletableFuture.runAsync(() -> handleLeaderboards());
    }
    
    public void updateWoolsLeaderboard() {
        WoolsPlacedLeaderboard woolsLeaderboard = WoolsPlacedLeaderboard.getActiveLeaderboard();
        if (woolsLeaderboard != null) {
            woolsLeaderboard.update();
        }
    }
    
    private void handleLeaderboards() {
        // Leaderboard de wools placements - visível para todos
        Location woolsLoc = getLocation("leaderboard_wools");
        
        if (woolsLoc != null) {
            new WoolsPlacedLeaderboard(woolsLoc).handle();
        }
        
        // Leaderboard de punições - carregado quando um staff entra
    }
    
    public void handleStaffLeaderboards(User user) {
        Account account = user.getAccount();
        Player player = account.player();
        
        if (player == null || !player.isOnline()) return;
        
        RankType rank = account.getRankType();
        boolean isStaff = rank == RankType.HELPER || rank == RankType.TRIAL || 
                         rank == RankType.MOD || rank == RankType.MODPLUS || 
                         rank == RankType.ADMIN || rank == RankType.CHEFE;
        
        if (!isStaff) return;
        
        // Verificar se o staff quer ver a leaderboard de punições
        if (!account.getToggle().isShowPunitionsLeaderboard()) {
            // Se a leaderboard estiver ativa, remover
            removeStaffLeaderboards(player);
            return;
        }
        
        com.minecraft.core.bukkit.manager.list.HologramManager manager = BukkitCore.getManager().getHologram();
        
        Location punicoesLoc = getLocation("leaderboard_punitions");
        if (punicoesLoc != null && manager.notExistsClient(player, "leaderboard_punitions")) {
            new TopPunitionsLeaderboard(player, punicoesLoc).handle();
        }
    }
    
    public void removeStaffLeaderboards(Player player) {
        com.minecraft.core.bukkit.manager.list.HologramManager manager = BukkitCore.getManager().getHologram();
        
        com.minecraft.core.bukkit.api.hologram.type.client.HologramClient client = 
                manager.getClient(player, "leaderboard_punitions");
        if (client != null) {
            manager.removeClient(client);
        }
    }

    @Override
    public void handleSidebar(User user) {
        Account account = user.getAccount();
        Sidebar sidebar = user.getSidebar();
        Player player = account.player();
        boolean isVanished = Vanish.has(player);

        sidebar.clear();
        
        sidebar.setTitle(com.minecraft.core.Constant.SERVER_NAME);
        if (sidebar.isAnimated()) {
            sidebar.setAnimation(new Scroller(com.minecraft.core.Constant.SERVER_NAME, "§6§l", "§f§l", "§6§l"));
        }
        
        sidebar.blankRow();
        sidebar.addRow("rank", "§fRank: " + account.getRank().getColoredName());
        sidebar.addRow("lobby", "§fLobby: §7#" + (account.getServer() != null ? account.getServer().getId() : "?"));
        sidebar.blankRow();
        sidebar.addRow("players", "§fPlayers: §b" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));
        
        if (isVanished) {
            sidebar.blankRow();
            sidebar.addRow("vanish", "§cVANISH ATIVADO!");
        }
        
        sidebar.blankRow();
        sidebar.addRow("website", "§e§o" + com.minecraft.core.Constant.SERVER_DOMAIN);
        sidebar.display();
        TagManager.updateTag(account);
        
        if (isVanished) {
            VanishParticlesTask.start(player);
        } else {
            VanishParticlesTask.stop(player);
        }
    }

    @Override
    public void handleEntities() {
        Core.getLogger().info("[MainArchitect] handleEntities() called");
        
        Location npcBw = getLocation("npc_bw");
        Location npcDuels = getLocation("npc_duels");
        Location npcEw = getLocation("npc_ew");
        Location npcSw = getLocation("npc_skywars");
        Location npcTb = getLocation("npc_tb");
        Location npcHg = getLocation("npc_hg");
        Location npcPvp = getLocation("npc_pvp");
        Location npcTreinoPonte = getLocation("npc_treino_ponte");
        
        Core.getLogger().info("[MainArchitect] All locations loaded, checking npc_shop...");
        
        Location npcShop = getLocation("npc_shop");
        if (npcShop != null) {
            Core.getLogger().info("[MainArchitect] NPC Shop location found: " + npcShop.getX() + ", " + npcShop.getY() + ", " + npcShop.getZ());
            handleCommandNpc("npc_shop", npcShop,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc3MDMxMzM4ODA0MCwKICAicHJvZmlsZUlkIiA6ICI0MDU4NDhjMmJjNTE0ZDhkOThkOTJkMGIwYzhiZDQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMaWFtX1NhZ2UiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk0NjZmZDc4ODg4NjY4NmRmNmQ0YWE2ODY2NjFmMTZlOTEyOTUxMDJjYjlkNDkzZmE0Mjk5YWQ3N2ZhYTRkYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "sLyL8L37+NLlfK+7aC2QXxjxdtniUQtRyumjpPY5VRH3oU7vd5xStS3vJU+XJDhzNhcXMuYHQ5npZ+vgNSx90c/+7Z7Qc4QxILFPatazY0QvT1WKgaaAx+BUrP32CySPWfoSfjdFdocJn+ssMqb2vpoPP2XvYeraN1tf8LbWuxKHVjQRLtHAFvPouVvB46hazDRrTtIziJ3LkRdr/afp/JMYcc21dmBLEGhGGZqIxhGwxq4lkotMPmB8osJY1Tg3FU9q8EW1VrvSeKGoyLZkLgj869f/AliGFyNBqUw76LQpTLpqFTpInuuuFnVWAFGVPvz/FaTN9izFH/NBhSwvnP6VDjXHcbMTi7JMN8jw2kFOQGUDESxRg2rYZlkOB3uWjbtpDEBIY5gOl/LkuYH6ouf+jAllYkK+T9xBBFsT6JTWtx1TPUpgDjYJNCztzjjffib8fzmdPqHjCZT085P1o67Azruy1AcOi3tuIU4JjUEte4SoeUnjstoABWWcNBNicF5oHknKbDVBoucelS9wbX6bSzMaZobfjt05GtlicCZZB9wvdp971UgFcaQLgq/wX5sGLkOkXFuHwb4xWySrKt8VM+Zq/b/E7vv0TOA6HcZzCNGPgwfHRNORCHk04lj/ZtGuKzA4CcX/svl5C2fCsEkX6Xr2oAfywQVMjLIF5pU=",
                    Arrays.asList("§d§l10% DE DESCONTO", "§6§lLOJA", "§eClique para ver!"),
                    "loja");

            handleShopRotatingBlocks(npcShop);

            HologramServer shopHolo = BukkitCore.getManager().getHologram().getServer("npc_shop");
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
        } else {
            Core.getLogger().warning("[MainArchitect] NPC Shop location NOT FOUND!");
        }
        
        // NPC Patch Logs
        Location npcPatch = getLocation("npc_patchlogs");
        if (npcPatch != null) {
            handlePatchLogsNpc(npcPatch);
        }

        // Social Discord - Holograma apenas
        Location npcDiscord = getLocation("social_discord");
        if (npcDiscord != null) {
            handleHologramClickWithMessage("social_discord", npcDiscord,
                    Arrays.asList("§9" + com.minecraft.core.Constant.SERVER_DISCORD.replace("https://", "")),
                    "§eAcesse já nosso discord! §9" + com.minecraft.core.Constant.SERVER_DISCORD);
        }

        // Social TikTok - Holograma apenas
        Location npcTiktok = getLocation("social_tiktok");
        if (npcTiktok != null) {
            handleHologramClickWithMessage("social_tiktok", npcTiktok,
                    Arrays.asList("§d" + com.minecraft.core.Constant.SERVER_TIKTOK),
                    "§eSiga-nos no TikTok! §d" + com.minecraft.core.Constant.SERVER_TIKTOK);
        }

if (npcBw != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcBw,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc0OTM5MzQ3NzU0MCwKICAicHJvZmlsZUlkIiA6ICJmOWQ0YzBkNDY2OWY0YTVkODg1MGZjYTNkMzQ0YjY2NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYWRhbmdHYXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcyZmNlNjRmMDI5OTUwZmM5MDI0ZWM4NzQzMTY5YjdhOTA3ZDQ3Y2U5ZTRkMTQ5YzlkYTkzM2NhMzYzYjgzYyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "cHweK3a+Voo4h6eaiGwUx8nDoOSAX4kZ9OLyZYJ5WmRKy3acayddm7fIQn0alU3Vwj3P7FWVmbRp5Q93N2+jL6AmLuyqabgqhROn1vlBSNHiWD3JiDKxA10YMXb0SBLDAUKvaHdj394g0nAdm9Wmpp/RDdC9xqHht4cmxGVqmrb6rEod18mBNXkiyG8nVoI/OM/G6XyR/fP2vWzPbhKLOkbot4pOIcb2ZIpPCCkyrsHcRmrqg2Mv9cXH7hcwdnkUnaeiiBmpWidUDb4JUYS5n2BQjx923smCRylHqyvp9fPvYCyAhWRv8ebH7SAP7Dh0ZrI7lCyUDLJ+d3CcUTPCkUMW/3Pz6rWEGPzLvNY6ES6W9xxNo4ZJH0Ys2r/y/2dCVxfaeSf0ZlScni98jNkK2jEANuckyshbYiksX11VZFwx6A0VA7KGucj+YPTgbw24y7lXVAgW600UASRM1zNJEER/KC63HS07+hc5NKnTFOCh8udseps6TVFM3WY4N86kU0b/131V3+ktIIhEY15mcL6sH+pF7y4G7oOtYZEuQGJi5c8xiB064xVUXViTniwAJO5IgmsDbvfgfHwytsCV0mhmLgWxWCglaMSDhmHoKeKlM8CyauP+YxGBgZquKj5D0zY/kWMl/+X3XGnqpR2z/87FY8DnX13ZIKIy0SdPQSQ=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_BEDWARS);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_BEDWARS.name().toLowerCase(), npcBw);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_BEDWARS);
            hologram.setText(Arrays.asList(
                    "§6§lBED WARS",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando."
            ));
        }

        if (npcDuels != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcDuels,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc3ODIyMzU1MTI3MCwKICAicHJvZmlsZUlkIiA6ICI5OGQxYTQyNmRlMmU0NjBkYjdjNWExMmY5MGNhODg0OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLdWJpbm9TSyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NDRjNGEwOGJmZjFhM2VjZDhhNTcwNjc0N2VkYzIxMWVhMTYzNDlkODE0Y2JiMjJhOWFiMzkzZmMwYmRkZDdkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "pmxZX4lG858v2QqJoQc7BRJK8EugO7f25KJ200pBP0we9VTO4ucfCkZo1c83EIFKVd+oh45gX/YShFgMiHT9F6rO3mI8Wp+dIOnCsA1GjO56WLzg1lpzKBifqmqRCdavaeQ+ClwJQiJoUGGMVkFQN2gN2pbTQVq6XOc9sYSG8lkukydlnfnP/10/zx7YmlsjULcRJPmoYbwu10lGksry83uSii/X/cGqvJbvKGCrXP/fLU87Zo/VrTUtRTtcOtwgMno5l1IwVVBMwd47x0AEPDVs5MlNBTz7ADCRXDwyoOogm8fgr331CNY60PQeWdiGb+9eDxV+lqNk1w96gGkVhgb11kd9+X7O7YKGjArbbAwub8Ua06SP/9vk8g+irphbVKeL1J3yClggkyJ89QCdiBFVWY2bVkkUD9uxHYBJlFYA8+2I/KEU48Lh3Qq/FvTLfYDhXijA9OEe2QYIISgiJeVNbKAxXA6lJr+GQPFPu8CpvBqyITihliKd7L/T3BjETzbrynTFbXQQtPuHnnGe2Su9IyoKECuU4Fd6xhRFOC5T47+NaMQuXK3I4TTXWvxi0xlyO/Bx29q84jfxLc5RJopmi0ODCZMVrdzQJmBpDB432WewbOfcBvBV7ItEyMCYcz5XFpWpCWUMW+d34mNcVnOYKHo386wTOtu27NOPrCM=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_DUELS);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_DUELS.name().toLowerCase(), npcDuels);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_DUELS);
            hologram.setText(Arrays.asList(
                    "§6§lDUELS",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando."
            ));
        }

        if (npcEw != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcEw,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyNjM3NzQ5NDc2OSwKICAicHJvZmlsZUlkIiA6ICJjMTJkMmY5ZWJhZGI0ZTllYTIxZmM2M2M3YWY3M2E5NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEcmVhbXlOZW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkNTU0OWYzNTczYWM2ZTRjYWQ0MDdjNTRhZWZjMjZkOTAwOTMzZmQ3MmQ5NGIxOTA4NTkyMzg5YTM0Yzg1MzEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "WCKPDLGRBjI6Mf0OlbDAjwbaxKlEiG9tZSGc5sVvYFfBlTsEYU6Ehdu9wbeNUX6h2T6Y/v8f98r5GSKuLhNTsSM2M2Ldx5NiGkmZrIWQKCE4fAUR24ozfojFStSuRlnpH0oz2FGzzNI9Tw39EXKBViEK79RqB83dhu/FIWq912AcGL4Y0mddC/HWPKbqsK5vfbur2C4/7FX/WNFmsMY6bOPT0o1+2BSJlDlDpGYffWFT7iUV8K9kJ6Alz4XPN0Cu5BMgQ7s94xa8JbxQ1BO4u77fANVKjXDRUwlp3GSuoS+InzicQeLselIq/Gw80L6YdkiHV5IWjaLVs3egMGK8Qdpp+H9Y6xqtyOknJfV71meshgc6nqGHb30ctqQOdaFZc5HNFGf08pKhcY4ZLZdv2CSzQ5kqNzv7CMh+iySrD1LCEk8xQvWQtgtS8vJ7uGTsrLsHbHCGnqF8N1IFyp8HJxgWxX4zuUj8bsHShtMK5mcXRolS39TbuL1ni8kWGOMajGHXlYsZNphgb6EDxGIt4gZhnrPOXVsnJ+Uu0JuynZ/Ak50qctealEEXFJVQZ6XLHVZzlqBjaToGfk7yHCYlQXYwrtQ6Ew/yHDM9JGoeZvqSMuAD96qd3NKXeMAQmcjH4PkpK8wzOsahrYOSeh6wnVJsoYiKpJQKqIyfFcY/tXg=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_EGGWARS);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_EGGWARS.name().toLowerCase(), npcEw);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_EGGWARS);
            hologram.setText(Arrays.asList(
                    "§6§lEGG WARS",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando."
            ));
        }

        if (npcSw != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcSw,
                    "ewogICJ0aW1lc3RhbXAiIDogMTY4OTgxMzk5MjgwMSwKICAicHJvZmlsZUlkIiA6ICJkNzU2OTc4MWUyYjY0OWIyYjVlMjVlYTJhNDZkOGQxOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEckthcGRvciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDQ2Yzk1NmVmOThhOTkyZmZmN2ZlNDkxZGQ4MWNlMTBmNTM5YTk1YjVlNzkzMWQ4ZjMzZDAwYjdkOTYzNjdjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "BmD50CTXr6FS78VcYs1fC5KW5Sh5m7I7WSYdfLdGLLihj923Jz3ZQSEruphISEPd6/ovNwPK1lzVQlHTKR6TCJnDVwUkS/pLx30HZFjA/vy7qebCu8GeEkEA8XGTbkj1noIMsKhFMs1rhuNzh6ZeAeMzxqodDBQVzuaSns/JXin8OuHxc6OAEPYCTIJ4cMKOkXTApR1BIHFL6HDa/xjbS9SbUDyNXioo5fIkwwHNgD+uvfz5YDBonbBTaWwxkcNjdrO69sTI9yudcpcm3ouDAaYKgMtVuAn08wgwKGvg6lNZrQWOSiO8l3YgHhpHTA8vSwdMetiy2jurRJVKBAF6EmmnBRtyYRdhVBccRk0TLG/A+XC5/PIk1Ix+N2f+xdIF6MsdXTynhWse+ACdxxocdLELy/5hQFf9bUw/nWwf+uCVjGPNUQ8tmrTGvic/pyU6a3fgUZFCYfa3jhYrI/86uhF85xNgYLXjnP+OwTLbJsrTXEJa+3hqXtmSWmJC5JdZdJrhHGKtBqShOx0mEzpbZr8ho6Etktzo95haGLs9aDyNkO5LUH+RuAQTOfwO5ZfeoP0cymxxx7wZtYOvMNTtw3BRVJwzB+1NKJjL3aflolePHn2AVoZ4hCE/Oy7dBZ3n/vj66gI7F91p7Q8nxiK0WrQq/pjAQt3wmwY+whbELc0=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_SKYWARS);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_SKYWARS.name().toLowerCase(), npcSw);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_SKYWARS);
            hologram.setText(Arrays.asList(
                    "§6§lSKY WARS",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando."
            ));
        }

        if (npcTb != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcTb,
                    "ewogICJ0aW1lc3RhbXAiIDogMTczNjA4Mjk4OTI1OCwKICAicHJvZmlsZUlkIiA6ICJhYzY1NDYwOWVkZjM0ODhmOTM0ZWNhMDRmNjlkNGIwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFjZUd1cmxTa3kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTI0NjQ2MTM0ZTljMmQzYTIwNDM5OGE2Mjk5ZjM0OTRiOGE4ZjI5Y2YwNTA1NjI5NGE0OGM0MGVmN2VlYTZlYyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "MgZhdUdFDIYYTs6Yd6vQtkiJwuGwgJ9y0QgzurSd4Zr6eDj+RugyU/tnuPhI4vh6oqQNndbMKJ8i2yAKFXnofuB5d/azVqZJqjkLk2RnvqLp7QiQEAw3B1AhKeDKQMsfChthRDAWx/xkRwVQT6VhdBFLmeNcTkJUbiHH/N5F37MuBFopFeu+ccqLP+U2Y+VkMgAY54fvteLMWRAWNYnpPuWCZLHYC5F6kY4h4C9TKFb8bGE4hxAcabDFJr8QgpBiel8vLN4/DKaRWsDi0HicVj9lrpQCbawOaVOC1rGgaLMXMLt+9urgj6fzfz43lshDOIxNxMZOWxm0beCHv2DrOFngvqCJA5lNpjcyeWar2Knt1SPU/Tt1j42UZFhlZN3uex9x62ZShH+vpPxL6ZTw0biGiRdXb1OSrtdOtMJAMjvLxyR7NQMlhIuwkHZ4ihYzR2eZQXbG19daHjyokBSTWevrGQq67qMbIzwpxeIuRj8YKdF09Db7HH4vXTJ6kDF0JtYZWFsx2NA10dO/USXjH5S4aIm5Q3WVDJDn0W19rI64YDgVE+OcNB0tsUEjZoO0vO7yBEKobPZaBQUqJh/Wr8GXFmTjori+pFDlX2lLKUb0RmMoX4IsDtLvWgn6LDWfWYtzxu4wKf56pB7+G6eMY6GNJCrBEDhLaqMjy29C4cY=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_THE_BRIDGE);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_THE_BRIDGE.name().toLowerCase(), npcTb);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_THE_BRIDGE);
            hologram.setText(Arrays.asList(
                    "§6§lTHE BRIDGE",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando."
            ));
        }

        if (npcHg != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcHg,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc3NDY0OTM3MDg5NCwKICAicHJvZmlsZUlkIiA6ICI4OGI1MWYwM2I4MDE0NTdhYjRkZDc0MTk4ZjNmNTI3ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYXRzdVB2UCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81M2UxYmI2YzE2Y2NiODViMmM3YzhlMjMyNTA5Mzg2NjI0NDhiZTNhZjM0NzhmYTgwOTJmY2FmNTBhMzVlYyIKICAgIH0KICB9Cn0=",
                    "NRsbuWAQEeEW8iQgOLoApsktYaBY/p/9VBGwau26J43xwvTAe+ymrQaSdoTUjPMpehK5lyQzGqqOoRUHZnD6yJchs8ReaxhgbWg6xYGIf1dudoJMV3iST1PiZ65kN/YIuSgBGokF+TLgbdtOT3uCA9PLJo7BiIfxtn4njZ3He/i2XXLAhAk+4q+HKGPrkm6HqmvZ2VD8e5iFrKfO/WX9xUA5i9KCB7VYMB3DaWXDk52LWCRl/Qh5XaSS4WWJBD3vF9/1Ku68Id2CtxevDA1nNVUZtrB0pGDu/I3rE1lhi3KwgzeW1jRy3Tfkmco4AzZAzOa8M0B9nnKYLP/sDZ0Rjb2Ilb64nwy4XxryTPYEk3qmGiT6ejAHza/9zxe5f4XutXpb9/ysD1RP3F3UzkgIlbIoS+MQug/RF8qAq2Yz9+2aidP65x0fSUpElvuqG3Ov/JcsoylK4ulzgK2q9QdtUT/m5fMujsepQ6lfqDVejSu+7wllvtxdZ1BphIHjuCeoeyXLnXKtG7QZkxdDMdDERSO+rP5hqB1dGCzm+5t6vV+cAJSq2iUfIuzSRH5UDgbqTDl3KyW+CeI3UIp4MBEyFpbyzNuhqDls9HjTOWy32vHcOWMhagl/drNIJCvYYXSTbtX6FKEuCEHTCRnVEwcmH9qApTSNrEk4ylRLJ8LSFgs=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_HUNGERGAMES);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_HUNGERGAMES.name().toLowerCase(), npcHg);
            hologram.setText(Arrays.asList(
                    "§6§lHUNGER GAMES",
                    "§e/jogar hg"
            ));
        }

        if (npcPvp != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcPvp,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYzNzMwOTI0MzMyMSwKICAicHJvZmlsZUlkIiA6ICIxYWZhZjc2NWI1ZGY0NjA3YmY3ZjY1ZGYzYWIwODhhOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3lfQmxvb2RBbmdlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NDYyMmVhMTVhNjM0MDA5NjRhYmFiODg1OTgwNmE0ZDViODJhYzA0NTQ4YmMwMmRjZDRjZjY5YTljYzIwMzU5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "vG1oQTeKQuMWObUNUzKjbacEdRxICxUSL3K2PR+l6zL15KoTAgSe7/diRez4+ePapkoa/sFzIOw4yFGCy4yPA0nrxYjivBtKYik1wS3RShJHbaRiiuqFAsZSw12WMXDqg7bUk+UwtZm9/sjVULfM4i0sSP/memXmIfNnjQFZSnZxfHlOJU5WabybyAqVpqNgahcGfz8bZMMwV6Mtd+nRYyHOm1wRFBjZZa63we4tTRc0rw7pOcvPY8I8xC2S9AASlvAcfuTqvdkzWYLW70CNkY5laZZANCj9OZsDvBaHq4YnP691qPszMZ2hdxEesBb6nCucSKvPNDUsAttwTxIjjjgWkQ4PIIxxJGyT34DGWokC4VIpOmCGN3rf3Xuc5tTLaKZvOKbDpiu+fEcls/3q5YzhK9BXWhF8YRGCcrPviDcwnllSEG1reta5grXzqxdaGWUqbZPHSUu0m5Vt9RY9OtDIAuSLaHJSdGYyUH5SfjGs+Fk/fD5JTebMa5EtQCPOE3hH+Du4hRd7Ka9HWRKrksyPQCDKi7rg5uysfLBHQia3P8JpGTSJTy+wETZ2uSNLRyhD/YvP4Y34SSEdirpmQcI/qwVfUxAmzEJTNE/PT/8eGIS7FpNhvQ6+bQJAXIFXnJkGZa6Q0Z1Bk+A+SR73oyca5CfSuSxj0BinXy5aXHc=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_PVP);
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_PVP.name().toLowerCase(), npcPvp);
            hologram.setText(Arrays.asList(
                    "§6§lPVP",
                    "§e/jogar pvp"
            ));
        }

        if (npcTreinoPonte != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcTreinoPonte,
                    "ewogICJ0aW1lc3RhbXAiIDogMTc3NjgxNTg0NjcyNSwKICAicHJvZmlsZUlkIiA6ICIxYjk1M2UzODUwZTI0NjIwYWMyYzg1Yzc2ZDQxZGNmYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXJib25Xcml0ZXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI2NzYzYmZkOThkMWQzNDkzZTUxZTYyN2Q2MDVjODFjOGNiOTBlZDk1OTFmYjAzOGQ2MTE4YTIyMjMxMWZjNyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "fqUwJQTAsNORCueiLFmYaoXA0v+HPss016bNCyGdP2PbY5PPGO6mXMNbLqzmj2PR368J3NaoF5JgbVCUgfJy/EpUE8gVLoMKvRPxbeIu9RVA7BXTOhdwXn0mKdRAbEHT4w34rIUGJT6M4yygLMjnGJ2i6nr+7AQte0dYr9+kyuTv3UVTDuBwAPy8496ZhpvkgzwfcC0DZHc4zmeGPjl83nx1hgiya0R6fENd6Y2q0GthcxEDfvdsYEq6xUFTCUWVstlDgdG7IP3fFkc8GCaryimmKgI8f+cYRPyzSKfALEmQPbn/xkOCnaEteCIhNMvJ1cRZUfqFRcyPD7i9FSdLiRJSh2XEIzqV7vkWrcuuWNg5dyU7BER5paIHHpyL2w6S6hpQzXS9NtkKuQ2lsF9/XBs/Zu2tlARsYkWljvfxWrlEJvyAG1hX1oG434oS6lMwb2c8o2i47u1hYecBQQsFm+jNqFMR+jqs3MCJ30VfGTqwPXQ6v6x7EM9JNSSv4wVGWJO2wu+ab2nzjyP5iRm75OQZqkAaMZG8zoai3AefSMypb+s1bnbT2HJ6h4noVP3VFn+/FQ/u0GZ1gLBgZGAY/gj1EQkj4KaSXuIQCxdFlxVQTdtkhXH/MLMQLCqKVvntSdPQbm+bn6ZC1L329tpeni0nK69w6ugWatciyMNmYW8=");

            npc.setContact(true);
            npc.setAction((player, action) -> {
                new TreinoPonteMenu(player).handle();
            });
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_TREINO_PONTE.name().toLowerCase(), npcTreinoPonte);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_TREINO_PONTE);
            hologram.setText(Arrays.asList(
                    "§6§lTREINO: PONTES",
                    "§e" + Util.formatNumber(totalPlayers) + " jogando!"
            ));
        }
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Core.getAccountController().list().forEach(target -> {
            StringBuilder builder = new StringBuilder();
            
            // Adicionar medalha se não for NONE
            if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                builder.append(account.getMedal().getColoredSymbol()).append(" ");
            }
            
            if (account.getTag() == com.minecraft.core.account.context.objects.tag.Tag.MAX_PLUS) {
                char colorChar = account.getMaxPlusColor();
                int level = account.getMaxPlusLevel();
                String maxColor = level == 2 ? "§b" : level == 3 ? "§6" : "§5";
                builder.append(maxColor).append("§lMAX§").append(colorChar).append("§l+ ").append(maxColor);
            } else {
                builder.append(account.getTag().getByPrefix(target.getTagPrefix()));
            }
            builder.append(account.getNickname()).append(": ");

            TextComponent text = new TextComponent(builder.toString());
            text.addExtra(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(text);
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(2.1).setY(0.6);
    }

    private int getTotalPlayersForLobby(ServerType server) {
        int onlinePlayers = Core.getServerData().getOnlinePlayers(server);
        ServerType realServer = server.getRealServerOfLobby();
        if (realServer != null)
            onlinePlayers += Core.getServerData().getOnlinePlayers(realServer);
        return onlinePlayers;
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ServerType server : ServerType.list(ServerType::isLobby)) {
                if (server.equals(ServerType.HUB) || server.equals(ServerType.HUB_TREINO_PONTE)) continue;

                HologramServer hologram = BukkitCore.getManager().getHologram().getServer(server.name().toLowerCase());

                if (hologram != null) {
                    int totalPlayers = getTotalPlayersForLobby(server);
                    hologram.setText(1, "§e" + Util.formatNumber(totalPlayers) + " jogando.");
                }
            }
        }
    }

}