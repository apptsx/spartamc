package com.minecraft.lobby.parkour;

import com.minecraft.core.Core;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ParkourManager {

    private static final Map<UUID, ParkourData> playerData = new HashMap<>();
    private static final Map<ServerType, ParkourCourse> courses = new HashMap<>();
    private static final Map<String, List<ArmorStand>> startHolograms = new HashMap<>();
    private static final Map<String, Map<Integer, List<ArmorStand>>> checkpointHolograms = new HashMap<>();
    private static BukkitTask parkourTimerTask;

    static {
        Location[] mainCheckpoints = {
                new Location(null, -28.5, 78.0, -27.5),
                new Location(null, 5.5, 94.0, 31.5),
                new Location(null, -14.5, 106.0, 75.5),
                new Location(null, -12.5, 115.0, 122.5)
        };
        courses.put(ServerType.HUB, new ParkourCourse(
                "HUB",
                new Location(null, 6.5, 61.0, -6.5),
                mainCheckpoints,
                new Location(null, 61.5, 131.0, 107.5)
        ));

        Location[] bedwarsCheckpoints = {
                new Location(null, 10.5, 70.0, 20.5),
                new Location(null, 30.5, 85.0, 50.5),
                new Location(null, 20.5, 95.0, 80.5),
                new Location(null, 40.5, 110.0, 100.5)
        };
        courses.put(ServerType.HUB_BEDWARS, new ParkourCourse(
                "HUB_BEDWARS",
                new Location(null, 5.5, 60.0, 5.5),
                bedwarsCheckpoints,
                new Location(null, 50.5, 120.0, 90.5)
        ));
    }

    public static void removeOtherCourses() {
        ServerType currentType = Core.getServerType();
        if (currentType == null) return;
        List<ServerType> toRemove = new ArrayList<>();
        for (ServerType type : courses.keySet()) {
            if (type != currentType) {
                toRemove.add(type);
            }
        }
        for (ServerType type : toRemove) {
            courses.remove(type);
        }
    }

    public static void initializeHolograms() {
        org.bukkit.World world = org.bukkit.Bukkit.getWorlds().get(0);
        if (world == null) {
            Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), ParkourManager::initializeHolograms, 100L);
            return;
        }

        removeOtherCourses();
        cleanupHolograms();
        cleanupOldHolograms();

        ServerType currentType = Core.getServerType();
        if (currentType != null) {
            ParkourCourse course = courses.get(currentType);
            if (course != null) {
                createHologramsForCourse(world, course);
            }
        }

        for (int delay : new int[]{100, 200, 400, 800, 1200}) {
            Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), ParkourManager::cleanupOldHolograms, delay);
        }
    }

    private static void cleanupOldHolograms() {
        List<ArmorStand> toRemove = new ArrayList<>();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) entity;
                    // Não remova hologramas com metadados (são os novos)
                    if (as.hasMetadata("parkour_hologram")) {
                        continue;
                    }
                    String name = as.getCustomName();
                    if (name != null) {
                        String strippedName = ChatColor.stripColor(name).toUpperCase();
                        if (strippedName.contains("PARKOUR") || strippedName.contains("CHECKPOINT") ||
                                strippedName.contains("INICIO") || strippedName.contains("RECORDE")) {
                            toRemove.add(as);
                        }
                    }
                }
            }
        }
        for (ArmorStand as : toRemove) {
            as.remove();
        }
    }

    private static void createHologramsForCourse(org.bukkit.World world, ParkourCourse course) {
        Location start = course.getStart();
        Location startLoc = new Location(world, start.getX(), start.getY() + 3, start.getZ());
        List<ArmorStand> startHoloList = createMultiLineHologram(startLoc,
                "\u00A76\u00A7lPARKOUR", "\u00A7aIn\u00EDcio", "\u00A7r ", "\u00A7eRecorde: \u00A77" +
                        (course.getBestTime() > 0 ? String.format("%.2f", course.getBestTime()) + "s" : "Nenhum"));
        startHolograms.put(course.getName(), startHoloList);

        Map<Integer, List<ArmorStand>> cpMap = new HashMap<>();
        for (int i = 1; i <= course.getTotalCheckpoints(); i++) {
            Location cp = course.getCheckpoint(i);
            Location cpLoc = new Location(world, cp.getX(), cp.getY() + 3, cp.getZ());
            List<ArmorStand> cpHoloList = createMultiLineHologram(cpLoc, "\u00A76\u00A7lPARKOUR", "\u00A7eCheckpoint \u00A7b" + i);
            cpMap.put(i, cpHoloList);
        }
        checkpointHolograms.put(course.getName(), cpMap);
    }

    public static final ItemStack RESET_ITEM = createItem(Material.BED, "\u00A7c\u00A7lResetar", "\u00A7Volte para o in\u00EDcio");
    public static final ItemStack LEAVE_ITEM = createItem(Material.BARRIER, "\u00A7c\u00A7lSair", "\u00A7Sair do parkour");
    public static final ItemStack CHECKPOINT_ITEM = createItem(Material.COMPASS, "\u00A7a\u00A7lCheckpoint", "\u00A7Voltar ao checkpoint");

    private static ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static void startParkour(Player player, ParkourCourse course) {
        ParkourData data = new ParkourData(course.getName());
        data.setCurrentCheckpoint(0);
        data.setStartTime(System.currentTimeMillis());
        data.saveInventory(player);
        playerData.put(player.getUniqueId(), data);

        player.getInventory().clear();
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType().name().contains("WOOL")) {
                player.getInventory().setItem(i, null);
            }
        }
        player.getInventory().setItem(3, RESET_ITEM);
        player.getInventory().setItem(4, LEAVE_ITEM);
        player.getInventory().setItem(5, CHECKPOINT_ITEM);
        player.updateInventory();

        player.setAllowFlight(false);
        player.setFlying(false);

        Location startLoc = course.getStart().clone();
        startLoc.setWorld(player.getWorld());
        startLoc.setYaw(-130);
        player.teleport(startLoc);

        startTimerTask();
        player.sendMessage("\u00A76\u00A7lPARKOUR \u00A77» \u00A7eParkour iniciado!");
    }

    public static boolean isInParkour(Player player) {
        return playerData.containsKey(player.getUniqueId());
    }

    private static void startTimerTask() {
        if (parkourTimerTask != null) return;

        parkourTimerTask = Bukkit.getScheduler().runTaskTimer(Lobby.getInstance(), () -> {
            for (UUID uuid : new HashSet<>(playerData.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    playerData.remove(uuid);
                    continue;
                }

                ParkourData data = playerData.get(uuid);
                if (data == null) continue;

                long timeTaken = System.currentTimeMillis() - data.getStartTime();
                double seconds = timeTaken / 1000.0;

                String timeText = String.format("\u00A7eTempo: \u00A7b%.2fs", seconds);
                sendActionBar(player, timeText);
            }

            if (playerData.isEmpty()) {
                stopTimerTask();
            }
        }, 0L, 10L);
    }

    private static void stopTimerTask() {
        if (parkourTimerTask != null) {
            parkourTimerTask.cancel();
            parkourTimerTask = null;
        }
    }

    public static void updateStartHologram(Player player, ParkourCourse course) {
        List<ArmorStand> startHoloList = startHolograms.get(course.getName());
        if (startHoloList != null && !startHoloList.isEmpty()) {
            updateMultiLineHologram(startHoloList,
                    "\u00A76\u00A7lPARKOUR", "\u00A7aIn\u00EDcio", "\u00A7r ",
                    "\u00A7eRecorde: \u00A77" + (course.getBestTime() > 0 ? String.format("%.2f", course.getBestTime()) + "s" : "Nenhum"));
        }
    }

    public static void leaveParkour(Player player) {
        ParkourData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.restoreInventory(player);
            playerData.remove(player.getUniqueId());
            sendActionBar(player, "");
            player.sendMessage("\u00A76\u00A7lPARKOUR \u00A77» \u00A7cVoc\u00EA saiu do parkour!");
        }
        if (playerData.isEmpty()) {
            stopTimerTask();
        }
    }

    public static void checkpointReached(Player player, int checkpoint, ParkourCourse course) {
        ParkourData data = playerData.get(player.getUniqueId());
        if (data == null) return;

        if (checkpoint > data.getCurrentCheckpoint()) {
            data.setCurrentCheckpoint(checkpoint);
            long timeTaken = System.currentTimeMillis() - data.getStartTime();
            double seconds = timeTaken / 1000.0;
            player.sendMessage("\u00A76\u00A7lPARKOUR \u00A77» \u00A7eVoc\u00EA alcan\u00E7ou o checkpoint \u00A7b#" + checkpoint + "\u00A7e em \u00A7b" + String.format("%.2f", seconds) + "s\u00A7e, \u00A7e\u00A7lParab\u00E9ns!");
        }
    }

    public static void finishParkour(Player player, ParkourCourse course) {
        ParkourData data = playerData.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("\u00A7cVoc\u00EA precisa come\u00E7ar o parkour primeiro!");
            return;
        }

        long timeTaken = System.currentTimeMillis() - data.getStartTime();
        double seconds = timeTaken / 1000.0;

        course.setBestTime(seconds);
        updateStartHologram(player, course);

        player.sendMessage("\u00A7a\u00A7lPARAB\u00C9NS! VOC\u00CA TERMINOU O PARKOUR!");
        player.sendMessage("\u00A7Tempo: \u00A7a" + String.format("%.2f", seconds) + " segundos");
        player.sendMessage("\u00A7Volte sempre para melhorar seu tempo!");

        data.restoreInventory(player);
        playerData.remove(player.getUniqueId());
        sendActionBar(player, "");

        if (playerData.isEmpty()) {
            stopTimerTask();
        }
    }

    public static ParkourCourse getCourseByLocation(Location loc) {
        for (ParkourCourse course : courses.values()) {
            if (isNear(course.getStart(), loc) || isNear(course.getFinish(), loc)) {
                return course;
            }
            for (int i = 1; i <= course.getTotalCheckpoints(); i++) {
                if (isNear(course.getCheckpoint(i), loc)) {
                    return course;
                }
            }
        }
        return null;
    }

    public static ParkourCourse getCourseByServerType(ServerType type) {
        return courses.get(type);
    }

    private static boolean isNear(Location loc1, Location loc2) {
        return Math.abs(loc1.getX() - loc2.getX()) < 2.0 &&
                Math.abs(loc1.getY() - loc2.getY()) < 2.0 &&
                Math.abs(loc1.getZ() - loc2.getZ()) < 2.0;
    }

    public static void removePlayer(Player player) {
        ParkourData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.restoreInventory(player);
            sendActionBar(player, "");
        }
        playerData.remove(player.getUniqueId());
        if (playerData.isEmpty()) {
            stopTimerTask();
        }
    }

    public static ParkourData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public static ParkourCourse getCourseByName(String name) {
        for (ParkourCourse course : courses.values()) {
            if (course.getName().equals(name)) {
                return course;
            }
        }
        return null;
    }

    public static int getCurrentCheckpoint(Player player) {
        ParkourData data = playerData.get(player.getUniqueId());
        return data != null ? data.getCurrentCheckpoint() : -1;
    }

    public static void cleanup() {
        for (UUID uuid : new HashMap<>(playerData).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                leaveParkour(player);
            }
        }
        cleanupHolograms();
    }

    public static void cleanupHolograms() {
        for (List<ArmorStand> stands : startHolograms.values()) {
            for (ArmorStand as : stands) {
                if (as != null && !as.isDead()) as.remove();
            }
        }
        startHolograms.clear();
        for (Map<Integer, List<ArmorStand>> map : checkpointHolograms.values()) {
            for (List<ArmorStand> stands : map.values()) {
                for (ArmorStand as : stands) {
                    if (as != null && !as.isDead()) as.remove();
                }
            }
        }
        checkpointHolograms.clear();
        cleanupOldHolograms();
    }

    private static List<ArmorStand> createMultiLineHologram(Location loc, String... lines) {
        List<ArmorStand> stands = new ArrayList<>();
        double yOffset = (lines.length - 1) * 0.25;
        for (String line : lines) {
            Location lineLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() + yOffset, loc.getZ());
            ArmorStand armorStand = lineLoc.getWorld().spawn(lineLoc, ArmorStand.class);
            armorStand.setCustomName(line);
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setMetadata("parkour_hologram", new FixedMetadataValue(Lobby.getInstance(), "true"));
            stands.add(armorStand);
            yOffset -= 0.25;
        }
        return stands;
    }

    private static void updateMultiLineHologram(List<ArmorStand> stands, String... lines) {
        for (int i = 0; i < stands.size() && i < lines.length; i++) {
            ArmorStand stand = stands.get(i);
            if (stand != null && !stand.isDead()) {
                stand.setCustomName(lines[i]);
            }
        }
    }

    private static void sendActionBar(Player player, String message) {
        try {
            String json = "{\"text\": \"" + message + "\"}";
            org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer craftPlayer =
                    (org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player;
            net.minecraft.server.v1_8_R3.PacketPlayOutChat packet = new net.minecraft.server.v1_8_R3.PacketPlayOutChat(
                    net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer.a(json),
                    (byte) 2
            );
            craftPlayer.getHandle().playerConnection.sendPacket(packet);
        } catch (Exception e) {
            // Ignore errors
        }
    }
}