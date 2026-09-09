package com.minecraft.core.bukkit.listener.anticheat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrandListener implements Listener {

    private static final Map<UUID, String> playerBrands = new HashMap<>();
    private static final Map<String, UUID> ipToAccount = new HashMap<>();

    public BrandListener() {
        registerPacketListener();
    }

    private void registerPacketListener() {
        BukkitCore.getManager().getProtocol().addPacketListener(new PacketAdapter(BukkitCore.getInstance(), PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                try {
                    String channel = event.getPacket().getStrings().read(0);
                    if (channel != null && (channel.equals("MC|Brand") || channel.equals("minecraft:brand"))) {
                        Player player = event.getPlayer();
                        UUID uuid = player.getUniqueId();
                        Object byteArray = event.getPacket().getSpecificModifier(byte[].class).read(0);
                        if (byteArray instanceof byte[]) {
                            String brand = new String((byte[]) byteArray, StandardCharsets.UTF_8);
                            if (brand != null && !brand.isEmpty()) {
                                playerBrands.put(uuid, formatBrand(brand));
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    private String formatBrand(String brand) {
        if (brand == null || brand.isEmpty()) return "Desconhecido";
        brand = brand.toLowerCase();
        if (brand.contains("lunar")) return "Lunar Client";
        if (brand.contains("badlion")) return "Badlion Client";
        if (brand.contains("cm")) return "CM Client";
        if (brand.contains("forge")) return "Forge";
        if (brand.contains("labymod") || brand.contains("laby")) return "LabyMod";
        if (brand.contains("vanilla") || brand.contains("default")) return "Vanilla";
        if (brand.contains("cheat") || brand.contains("hack")) return "SUSPEITO: " + brand;
        return brand.length() > 20 ? brand.substring(0, 20) : brand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Core.getPlatform().runAsync(() -> {
            Account account = Core.getAccountController().of(uuid);
            if (account == null) return;
            String ipAddress = player.getAddress().getAddress().getHostAddress();
            account.setIpAddress(ipAddress);
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            String brand = playerBrands.getOrDefault(uuid, "Desconhecido");
            sendBrandAlert(player, account, brand, ipAddress);
            ipToAccount.put(ipAddress, uuid);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playerBrands.remove(uuid);
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        ipToAccount.remove(ip);
    }

    private void sendBrandAlert(Player player, Account account, String brand, String ipAddress) {
        String playerName = player.getName();
        if (account.getContext().getLastLogin() == 0) {
            sendToStaff("§c[ANTICHEAT] §f" + playerName + " §centrou pela primeira vez.");
        }
        UUID altUuid = findAltAccount(ipAddress, account.getId());
        if (altUuid != null) {
            Account altAcc = Core.getAccountController().of(altUuid);
            if (altAcc != null) {
                String altName = altAcc.getName();
                String status = getAccountStatus(altAcc);
                sendToStaff("§c[ANTICHEAT] §f" + playerName + " §centrou no servidor §7(Conta alternativa §8§l" + altName + "§7 " + status + "§7)");
            }
        }
        sendToStaff("§c[ANTICHEAT] §f" + playerName + " §centrou no servidor utilizando §6" + brand);
    }

    private UUID findAltAccount(String ipAddress, UUID currentUuid) {
        UUID altUuid = ipToAccount.get(ipAddress);
        if (altUuid != null && !altUuid.equals(currentUuid)) return altUuid;
        return null;
    }

    private String getAccountStatus(Account account) {
        StringBuilder status = new StringBuilder();
        if (!account.getHistory().getPunishments(p -> p.getCategory().name().equals("BAN") && p.isValid()).isEmpty()) {
            status.append("banida");
        }
        if (!account.getHistory().getPunishments(p -> p.getCategory().name().equals("MUTE") && p.isValid()).isEmpty()) {
            if (status.length() > 0) status.append("/");
            status.append("mutada");
        }
        return status.length() > 0 ? status.toString() : "limpa";
    }

    private void sendToStaff(String message) {
        Core.getAccountController().filter(acc -> acc.isStaffer() && acc.isOnline()).forEach(staff -> {
            if (staff.isOnline()) staff.send(message);
        });
    }

    public static String getPlayerBrand(UUID uuid) {
        return playerBrands.getOrDefault(uuid, "Desconhecido");
    }
}
