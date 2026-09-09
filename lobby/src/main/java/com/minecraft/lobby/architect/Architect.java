package com.minecraft.lobby.architect;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.tag.role.TagRole;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.touch.TouchHandler;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.menu.account.AccountMenu;
import com.minecraft.core.bukkit.menu.account.stats.StatsInfoMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.core.bukkit.menu.server.collectible.CollectibleMenu;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.WorldUtil;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.lobby.listener.WoolBlockListener;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.menu.arcade.ArcadeSlotMenu;
import com.minecraft.lobby.menu.cosmetics.TitlesMenu;
import com.minecraft.lobby.menu.navigation.ServersMenu;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@IgnoreEvent
public abstract class Architect implements Listener {

    private final String id = StringUtil.generateLetterCode(4) + Core.getServerId();
    private final ServerType server;

    private final List<SignedLocation> locations = new ArrayList<>();

    private com.minecraft.lobby.util.ShopSpiralTask shopSpiralTask;

    public Architect(ServerType server) {
        this.server = server;
    }

    public String getId() {
        return id;
    }

    public ServerType getServer() {
        return server;
    }

    public List<SignedLocation> getLocations() {
        return locations;
    }

    public void load() {
        handleEntities();

        WorldUtil.setup(getWorld());

        Bukkit.getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    public void unload() {
        if (shopSpiralTask != null) {
            shopSpiralTask.stop();
            shopSpiralTask = null;
        }
    }

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        handleSidebar(user);
        handleHotbar(player);
        sendTabList(player);

        // Teleportar para o spawn
        Location spawn = getLocation("spawn");
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            // Se não houver spawn configurado, usar o spawn padrão do mundo
            player.teleport(player.getWorld().getSpawnLocation());
        }

        // Garantir que o player não esteja voando
        player.setAllowFlight(false);
        player.setFlying(false);

// Verificar auto vanish
        boolean isAutoVanish = account.getToggle().isAutoVanish() && 
                               account.getRankType().ordinal() >= RankType.HELPER.ordinal();

// Verificar tag - tag MEMBER não tem mensagem de entrada
        Tag tag = account.getTag();
        boolean isMemberTag = tag == com.minecraft.core.account.context.objects.tag.Tag.MEMBER;
        
        if (!isMemberTag && !isAutoVanish) {
            // Pegar a mensagem de entrada selecionada pelo jogador
            String rawMessage = account.getJoinMessage();
            
            // Se não tiver mensagem selecionada ou for padrão, usar uma das mensagens padrão em laranja
            final String message;
            if (rawMessage == null || rawMessage.isEmpty() || net.md_5.bungee.api.ChatColor.stripColor(rawMessage).equals("entrou no lobby!")) {
                String[] messages = {
                    "§6entrou no lobby!"
                };
                message = messages[Core.RANDOM.nextInt(messages.length)];
            } else {
                message = rawMessage;
            }
            
             final Account finalAccount = account;
             final Tag finalTag = tag;
             
            // Verificar se o rank é Max+ ou superior para mensagem destacada
            // Usar a hierarquia de ranks (ordinal) para incluir ULTRA, CHEFE, etc.
            boolean isMaxPlusOrHigher = finalAccount.getRankType().ordinal() >= RankType.MAX_PLUS.ordinal();
            
            if (isMaxPlusOrHigher) {
                // Construir display da tag
                String tagDisplay;
                
                // Se estiver usando a tag MAX+, aplicar cores personalizadas
                if (finalTag == com.minecraft.core.account.context.objects.tag.Tag.MAX_PLUS) {
                    char colorChar = finalAccount.getMaxPlusColor();
                    int level = finalAccount.getMaxPlusLevel();
                    String maxColor;
                    if (level == 2) {
                        maxColor = "§b"; // Azul para nível 2
                    } else if (level == 3) {
                        maxColor = "§6"; // Dourado para nível 3
                    } else {
                        maxColor = "§5"; // Roxo para nível 1 (padrão)
                    }
                    tagDisplay = maxColor + "§lMAX§" + colorChar + "§l+ " + maxColor;
                } else {
                    // Outras tags (ULTRA, CHEFE, etc.) usam o formato normal
                    tagDisplay = finalTag.getByPrefix(finalAccount.getTagPrefix());
                }
                
                String fullMessage = tagDisplay + finalAccount.getNickname() + " " + message;

                int totalLength = net.md_5.bungee.api.ChatColor.stripColor(fullMessage).length();
                int borderLength = Math.max(40, totalLength + 10);

                String[] rainbowColors = {"§6", "§e", "§a", "§b", "§d", "§5", "§c", "§6", "§e", "§a", "§b", "§d"};
                StringBuilder border = new StringBuilder();
                for (int i = 0; i < borderLength; i++) {
                    border.append(rainbowColors[i % rainbowColors.length]).append("§m-");
                }

                String borderLine = border.toString();

                Core.getAccountController().list().forEach(target -> {
                    target.send("");
                    target.send(borderLine);
                    target.send("");
                    target.send(fullMessage);
                    target.send("");
                    target.send(borderLine);
                    target.send("");
                });
            } else {
                // Para outros ranks, usar a tag atual do jogador que entrou
                String tagDisplay = finalTag.getByPrefix(finalAccount.getTagPrefix());
                Core.getAccountController().list().forEach(target -> target.send(tagDisplay + finalAccount.getNickname() + " " + message));
            }
        }

        /* Aplicar configuração de visibilidade de jogadores */
        int visibilityMode = account.getToggle().getVisibilityMode();
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online.equals(player)) return;
            
            switch (visibilityMode) {
                case 0: // ON - mostrar todos
                    player.showPlayer(online);
                    break;
                case 1: // OFF - esconder todos
                    player.hidePlayer(online);
                    break;
                case 2: // RANK_ONLY - mostrar apenas com rank
                    Account onlineAccount = Core.getAccountController().of(online.getUniqueId());
                    if (onlineAccount != null && onlineAccount.getRankType().ordinal() > 0) {
                        player.showPlayer(online);
                    } else {
                        player.hidePlayer(online);
                    }
                    break;
                case 3: // FRIENDS - mostrar apenas amigos
                    if (account.getFriends().contains(online.getUniqueId())) {
                        player.showPlayer(online);
                    } else {
                        player.hidePlayer(online);
                    }
                    break;
            }
        });

        Location location = getLocation("npc_stats");

        /* Renderizar NPC de suas estatísticas */
        if (location != null) {
            CompletableFuture.runAsync(() -> {
                NpcClient stats = BukkitCore.getManager().getNpc().spawnClient(player, location,
                        account.getSkin().getValue(), account.getSkin().getSignature());

                stats.setContact(true);
                stats.setAction((target, action) -> {
                    Account other = Core.getAccountController().of(target.getUniqueId());

                    if (other != null) {
                        ServerType server = this.server.getRealServerOfLobby();

                        if (server == null) return;

                        if (server.equals(ServerType.BEDWARS))
                            new BedWarsStatsMenu(target, null).handle();
                        else
                            new StatsInfoMenu(target, other, server, null).handle();
                    }
                });

                stats.display();
            });
        }
        
        handleStaffLeaderboards(user);
    }

    public void quit(Player player) {
        removeStaffLeaderboards(player);
    }

    public abstract void handleSidebar(User user);

    public void handleStaffLeaderboards(User user) {
        // Default vazio - sobrescrito apenas no MainArchitect
    }
    
    public void removeStaffLeaderboards(Player player) {
        // Default vazio - sobrescrito apenas no MainArchitect
    }

    public void chat(Player player, String message) {

    }

    public void handleEntities() {
    }

    public Vector getSlimeJump(Vector direction) {
        return new Vector(0, 0, 0);
    }

    public Location getLocation(String name) {
        Location loc = locations.stream()
                .filter(location -> location.getName().equalsIgnoreCase(name))
                .map(location -> location.getSynthetic().of(Bukkit.getWorlds().get(0)))
                .findFirst()
                .orElse(null);
        
        if (name.equals("npc_shop")) {
            Core.getLogger().info("[Architect] Looking for location: " + name + " - Found: " + (loc != null ? loc.getX() + "," + loc.getY() + "," + loc.getZ() : "NULL"));
            for (SignedLocation sl : locations) {
                Core.getLogger().info("[Architect] Available location: " + sl.getName());
            }
        }
        
        return loc;
    }

    public List<SignedLocation> getLocationsByName(String name) {
        return locations.stream()
                .filter(location -> location.getName().startsWith(name))
                .collect(Collectors.toList());
    }

    public List<Location> getLocationsByKey(String... names) {
        List<Location> list = new ArrayList<>();

        for (String name : names) {
            Location location = getLocation(name);

            if (location != null)
                list.add(location);
        }

        return list;
    }

    public void sendTabList(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        
        // Se o jogador não estiver autenticado (state = PENDENT), não mostrar no tab
        if (account != null && account.getAuth() != null && 
            account.getAuth().getState() != com.minecraft.core.account.context.objects.auth.state.AuthState.OK) {
            // Não mostrar no tablist - remover entry
            return;
        }
        
        // Atualizar tablist para players autenticados
        for (Player online : Bukkit.getOnlinePlayers()) {
            Account onlineAccount = Core.getAccountController().of(online.getUniqueId());
            
            // Se o jogador não estiver autenticado, não mostrar no TAB
            if (onlineAccount != null && onlineAccount.getAuth() != null && 
                onlineAccount.getAuth().getState() != com.minecraft.core.account.context.objects.auth.state.AuthState.OK) {
                player.hidePlayer(online);
            } else {
                player.showPlayer(online);
            }
        }
        
        net.md_5.bungee.api.chat.TextComponent header = new net.md_5.bungee.api.chat.TextComponent(
            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', "&6&l" + com.minecraft.core.Constant.SERVER_NAME.toUpperCase())
        );
        net.md_5.bungee.api.chat.TextComponent footer = new net.md_5.bungee.api.chat.TextComponent(
            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                "&6Website: &e" + com.minecraft.core.Constant.SERVER_DOMAIN + "\n&6Loja: &e" + com.minecraft.core.Constant.SERVER_STORE + "\n&6Discord: &e" + com.minecraft.core.Constant.SERVER_DISCORD)
        );
        player.setPlayerListHeaderFooter(header, footer);
    }

    public void handleHotbar(Player player) {
        // Usar runTaskLater para garantir que o inventário esteja totalmente carregado
        Core.getPlatform().runSync(() -> {
            if (!player.isOnline()) return;
            
            User user = (User) User.of(player.getUniqueId());
            if (user == null) return;

            Account account = user.getAccount();
            if (account == null) return;
            
            Toggle toggle = account.getToggle();

            PlayerInventory inv = player.getInventory();

            inv.clear();
            inv.setArmorContents(null);

            inv.setItem(0, Item.of(Material.COMPASS, "§6Modos de jogo")
                    .interact(event -> new ServersMenu(event.getPlayer(), ServersMenu.NavigationType.SERVER).handle()));

            inv.setItem(1, Item.of(Material.SKULL_ITEM, 3, "§6Sua conta")
                    .skullByBase64(account.getSkin().getValue())
                    .interact(event -> new AccountMenu(event.getPlayer()).handle()));

            if (toggle.isShowColoredBlocks() && !Vanish.has(player)) {
                WoolBlockListener.startColorCycle(player);
                if (account.hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.VIP)) {
                    inv.setItem(3, new ItemStack(Material.WOOL, 64));
                } else {
                    inv.setItem(3, new ItemStack(Material.WOOL, 1));
                }
            }

            inv.setItem(4, Item.of(Material.CHEST, "§6Cosméticos")
                    .interact(event -> new CollectibleMenu(event.getPlayer()).handle()));

            // Configurar item de visibilidade baseado no modo atual
            int visibilityMode = toggle.getVisibilityMode();
            String visibilityName;
            int dyeColor;

            switch (visibilityMode) {
                case 0: // ON
                    visibilityName = "§aVisiveis";
                    dyeColor = 10; // Green
                    break;
                case 1: // OFF
                    visibilityName = "§cInvisiveis";
                    dyeColor = 8; // Gray
                    break;
                case 2: // RANK_ONLY
                    visibilityName = "§dJogadores com rank";
                    dyeColor = 9; // Pink/Magenta
                    break;
                case 3: // FRIENDS
                    visibilityName = "§9Amigos";
                    dyeColor = 12; // Blue
                    break;
                default:
                    visibilityName = "§aVisiveis";
                    dyeColor = 10;
            }

            inv.setItem(7, Item.of(Material.INK_SACK, dyeColor,
                    "§fPlayers: " + visibilityName));

            inv.setItem(8, Item.of(Material.NETHER_STAR, "§bLobbies")
                    .interact(event -> new ServersMenu(event.getPlayer(), ServersMenu.NavigationType.ROOM).handle()));
            
            player.updateInventory();
        }, 2L);
    }

    public void handleStatsNpc(Location location) {
        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("stats_" + this.server.name().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§lSUAS ESTATÍSTICAS",
                "§eClique para ver"
        ));
    }

    public NpcServer handleNpc(String name, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(name.toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + name.toUpperCase(),
                "§e" + Util.formatNumber(0) + " jogando."
        ));

        return npc;
    }

    public void handleServerNpc(ServerType server, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setAction((player, action) -> {
            User user = (User) User.of(player.getUniqueId());

            if (user != null)
                user.getAccount().redirect(server);
        });

        npc.setContact(true);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(server.name().toLowerCase(), location);

        if (server.getServerLobby() == ServerType.HUB_BEDWARS) {
            hologram.setText(Arrays.asList(
                    "§6§lBED WARS",
                    "§e" + Util.formatNumber(Core.getServerData().getOnlinePlayers(server)) + " jogando."
            ));
        } else {
            hologram.setText(Arrays.asList(
                    "§6§l" + server.getName().toUpperCase(),
                    "§e" + Util.formatNumber(Core.getServerData().getOnlinePlayers(server)) + " jogando."
            ));
        }
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, NpcAction action, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction(action);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + (arcade.equals(ArcadeCategory.NONE) ? "?????" : arcade.getName().toUpperCase()),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        handleArcadeNpc(arcade, location, (player, action) -> new ArcadeSlotMenu(player, null, arcade).handle(), value, signature);
    }

    public void handleCommandNpc(String hologramId, Location location, String value, String signature, java.util.List<String> hologramLines, String command) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction((player, action) -> player.performCommand(command));

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(hologramId, location);
        hologram.setText(hologramLines);
    }

    public void handleHologramClickWithMessage(String hologramId, Location location, java.util.List<String> hologramLines, String message) {
        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(hologramId, location);
        hologram.setText(hologramLines);

        hologram.setTouch((player, touch) -> player.sendMessage(Util.color(message)));
    }

    public void handleHologramClickWithAction(String hologramId, Location location, java.util.List<String> hologramLines, TouchHandler action) {
        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(hologramId, location);
        hologram.setText(hologramLines);

        hologram.setTouch(action);
    }

    public void handleShopRotatingBlocks(Location npcLocation) {
        if (npcLocation == null || npcLocation.getWorld() == null) return;

        org.bukkit.entity.ArmorStand diamondStand = npcLocation.getWorld().spawn(npcLocation.clone().add(1, 1.5, 0), org.bukkit.entity.ArmorStand.class);
        diamondStand.setGravity(false);
        diamondStand.setVisible(false);
        diamondStand.setSmall(true);
        diamondStand.setBasePlate(false);
        diamondStand.setMarker(true);
        diamondStand.getEquipment().setHelmet(com.minecraft.core.api.item.Item.of(org.bukkit.Material.DIAMOND_BLOCK));
        diamondStand.setCustomName("shop_diamond");
        diamondStand.setCustomNameVisible(false);

        org.bukkit.entity.ArmorStand emeraldStand = npcLocation.getWorld().spawn(npcLocation.clone().add(-1, 1.5, 0), org.bukkit.entity.ArmorStand.class);
        emeraldStand.setGravity(false);
        emeraldStand.setVisible(false);
        emeraldStand.setSmall(true);
        emeraldStand.setBasePlate(false);
        emeraldStand.setMarker(true);
        emeraldStand.getEquipment().setHelmet(com.minecraft.core.api.item.Item.of(org.bukkit.Material.EMERALD_BLOCK));
        emeraldStand.setCustomName("shop_emerald");
        emeraldStand.setCustomNameVisible(false);

        org.bukkit.scheduler.BukkitRunnable runnable = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!emeraldStand.isValid() || !diamondStand.isValid()) {
                    cancel();
                    return;
                }

                int random = new java.util.Random().nextInt(17);
                diamondStand.getEquipment().setHelmet(com.minecraft.core.api.item.Item.of(org.bukkit.Material.WOOL, random));
                emeraldStand.getEquipment().setHelmet(com.minecraft.core.api.item.Item.of(org.bukkit.Material.WOOL, random));
            }
        };
        runnable.runTaskTimer(com.minecraft.lobby.Lobby.getInstance(), 20L, 10L);

        shopSpiralTask = new com.minecraft.lobby.util.ShopSpiralTask(npcLocation);
        shopSpiralTask.start();
    }

    public void handlePatchLogsNpc(Location npcLocation) {
        if (npcLocation == null || npcLocation.getWorld() == null) return;

        String skinData = "ewogICJ0aW1lc3RhbXAiIDogMTc0MzUxODYyNjU3NSwKICAicHJvZmlsZUlkIiA6ICIwMTIxYzFjZDUxOTM0M2NmYWM4YzgyZThiNjVjYTFjZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfVmV4X1RWIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NjE0NzM4ZGU1MDUyM2YwMzFkZDU2N2Q0NGM3YTAzYWNhMTYwNGNhMjM3NmM1ZmFiNzljMjU0YmQ0YzNiNDMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
        String skinSignature = "J4evVudLFliYLcT/ZkArGlamW0uqdN3OgGqHFmgnvfG2ON4Ay/HRAgQpOdxQONrTiy4Gc4b2Pouf+YYUkWOx1IEq/zSCKcGYqAq0ro6Ib0A8jUYyPcngOA9OfHuEVnruqcQB1fQlErx9FHHdptF/3rtSFURy2klXg/Sl1x1AZkRVZmK56AU0UxG1YcslD8gyfLaYSoyU7tr5uicputISerWgs6SqgR2Pw1juvaoBvL0BwBkzkzNYyW8T0C4ed4Zr5BNMXsqZZmXUOtDhFo6OyzbI/+j1DFkKDtHbvx4gcnbK04Im+Bk4t5y84R5/zV8biYUy58de/6f+6fVAQUjPiOccAHL7Y/dBr/q7bcH97aP7T9DZwX3JZtaCAw67xORneKPJMlZCvCpDFht9Kf6sRHeS7pt9kT9E1txFcai8LfrFHUklB/a/S9P3QSgMIeYWv635nHUS2ICkktmdS09RADfA6FbdmTo59wMOW7IOCWto5xzCRNqqu1D5Leg7R2Rm7JYv8+nqL/TnXyTk6AM5mkJQ91/Gn76ra6QBgeAaaQb1eDc51VuIuHS5VhcVWOWKh0eyDShQTNobo+PjLU63ZLrdKHWR0307t4xrfliQQ1rx8t5X6SlR1p7QH711k1v77uXE0pTdlKL6aa2HjPN5bTwc600mrDes+B7n0YlBCMY=";

        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcLocation, skinData, skinSignature);

        npc.setContact(true);
        npc.setAction((player, action) -> {
            new com.minecraft.lobby.menu.patch.PatchLogsMenu(player).handle();
        });

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("npc_patchlogs", npcLocation);
        hologram.setText(Arrays.asList("§6§lPATCH LOGS", "§eClique para conferir!"));
    }
}