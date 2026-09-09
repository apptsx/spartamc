package com.minecraft.core.bukkit.listener.anticheat;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.database.redis.message.types.account.AccountACFlagMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@AllArgsConstructor
public class AntiCheatAPI {

    public enum HackType {
        SPEED("SpeedHack", 10),
        FLY("Fly", 10),
        REACH("Reach", 15),
        AIM("AimAssist", 10),
        TIMER("Timer", 10),
        SCAFFOLD("Scaffold", 15),
        VELOCITY("Velocity", 10),
        ANTIVELOCITY("AntiKnockback", 10),
        NOSLOW_FAST("NoSlow", 10),
        PACKET("BadPackets", 15),
        CRITICAL("Criticals", 10),
        Jesus("Jesus", 10),
        STEP("Step", 10),
        ZOOM("Zoom", 10);

        private final String displayName;
        private final int maxViolations;

        HackType(String displayName, int maxViolations) {
            this.displayName = displayName;
            this.maxViolations = maxViolations;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getMaxViolations() {
            return maxViolations;
        }
    }

    private static final Map<UUID, Map<HackType, Integer>> violations = new HashMap<>();
    private static final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_DELAY = 3000;

    public static void addViolation(Player player, HackType hackType) {
        if (player == null) return;
        
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null && account.isStaffer()) return;

        UUID uuid = player.getUniqueId();
        
        if (!violations.containsKey(uuid)) {
            violations.put(uuid, new HashMap<>());
        }

        Map<HackType, Integer> playerViolations = violations.get(uuid);
        int current = playerViolations.getOrDefault(hackType, 0);
        int newViolations = current + 1;
        playerViolations.put(hackType, newViolations);

        int totalViolations = playerViolations.values().stream().mapToInt(Integer::intValue).sum();
        int maxVL = hackType.getMaxViolations();

        String alertKey = uuid.toString() + ":" + hackType.name();
        long now = System.currentTimeMillis();
        Long lastAlert = lastAlertTime.get(alertKey);
        
        if (lastAlert == null || (now - lastAlert) >= ALERT_DELAY) {
            sendFlagAlert(player, hackType, newViolations, maxVL);
            lastAlertTime.put(alertKey, now);
        }

        Core.getLogger().warning("[AntiCheat] " + player.getName() + " - " + hackType.getDisplayName() + ": " + newViolations + "/" + maxVL);
    }

    private static void sendFlagAlert(Player player, HackType hackType, int violations, int maxVL) {
        TextComponent message = new TextComponent("§c[ANTICHEAT] ");

        TextComponent playerName = new TextComponent("§c§o" + player.getName() + " ");
        playerName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + player.getName()));
        playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique para ir até o jogador")));

        TextComponent alert = new TextComponent("§ffalhou §c" + hackType.getDisplayName() + " §7(VL: " + violations + "/" + maxVL + ")");

        message.addExtra(playerName);
        message.addExtra(alert);

        new AccountACFlagMessage(player.getName(), hackType.getDisplayName(), violations, maxVL).send();

        Core.getAccountController().filter(acc -> acc.isStaffer() && acc.isOnline() && acc.getToggle().isAllowACFlags()).forEach(staff -> {
            if (staff.isOnline()) {
                staff.send(message);
            }
        });
    }

    public static void reduceViolation(Player player, HackType hackType) {
        UUID uuid = player.getUniqueId();
        if (violations.containsKey(uuid)) {
            Map<HackType, Integer> playerViolations = violations.get(uuid);
            int current = playerViolations.getOrDefault(hackType, 0);
            if (current > 0) {
                playerViolations.put(hackType, current - 1);
            }
        }
    }

    public static void reduceAllViolations(Player player) {
        UUID uuid = player.getUniqueId();
        if (violations.containsKey(uuid)) {
            Map<HackType, Integer> playerViolations = violations.get(uuid);
            playerViolations.replaceAll((k, v) -> Math.max(0, v - 1));
        }
    }

    public static int getViolations(Player player, HackType hackType) {
        UUID uuid = player.getUniqueId();
        if (violations.containsKey(uuid)) {
            return violations.get(uuid).getOrDefault(hackType, 0);
        }
        return 0;
    }

    public static int getTotalViolations(Player player) {
        UUID uuid = player.getUniqueId();
        if (violations.containsKey(uuid)) {
            return violations.get(uuid).values().stream().mapToInt(Integer::intValue).sum();
        }
        return 0;
    }

    public static void removePlayer(UUID uuid) {
        violations.remove(uuid);
    }
}