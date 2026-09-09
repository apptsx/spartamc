package com.minecraft.core.bungee.service.discord;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.util.list.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.ProxyServer;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordService {

    private static DiscordService instance;
    private static final Random RANDOM = new Random();
    private static final String[] REACTION_EMOJIS = {"✅", "👍", "❤️", "🎮", "⚔️", "🛡️", "🏆", "⭐", "🔹", "🟢", "🟦", "💚", "💙", "💜", "🔶", "🔷"};
    private JDA jda;
    private ScheduledExecutorService scheduler;
    private String punishmentsChannel = System.getenv().getOrDefault("SPARTA_DISCORD_PUNISHMENTS_CHANNEL", "");
    private String ranksChannel = System.getenv().getOrDefault("SPARTA_DISCORD_RANKS_CHANNEL", "");
    private String staffChatChannel = System.getenv().getOrDefault("SPARTA_DISCORD_STAFF_CHAT_CHANNEL", "");
    private String shopOrdersChannel = System.getenv().getOrDefault("SPARTA_DISCORD_SHOP_CHANNEL", "");
    private String hubMainChannel = System.getenv().getOrDefault("SPARTA_DISCORD_HUB_CHANNEL", "");
    private String reportsChannel = System.getenv().getOrDefault("SPARTA_DISCORD_REPORTS_CHANNEL", "");
    private String activityText = "{players} jogando!";
    
    // Canais de chat normal por modo
    private Map<String, String> lobbyChatChannels = new ConcurrentHashMap<>();
    
    private DiscordService() {
        // Inicializar canais de chat por modo
        if (!hubMainChannel.isEmpty()) lobbyChatChannels.put("HUB", hubMainChannel);
        String bedwars = System.getenv().getOrDefault("SPARTA_DISCORD_BEDWARS_CHANNEL", "");
        if (!bedwars.isEmpty()) lobbyChatChannels.put("BEDWARS", bedwars);
        String pvp = System.getenv().getOrDefault("SPARTA_DISCORD_PVP_CHANNEL", "");
        if (!pvp.isEmpty()) lobbyChatChannels.put("PVP", pvp);
        String hungryGames = System.getenv().getOrDefault("SPARTA_DISCORD_HUNGERGAMES_CHANNEL", "");
        if (!hungryGames.isEmpty()) lobbyChatChannels.put("HUNGERGAMES", hungryGames);
    }

    public static DiscordService getInstance() {
        if (instance == null) {
            instance = new DiscordService();
        }
        return instance;
    }

    public void start() {
        String botToken = com.minecraft.core.Constant.DISCORD_BOT_TOKEN;
        if (botToken == null || botToken.isEmpty()) {
            Core.getLogger().warning("[Discord] Nenhum token configurado. Defina SPARTA_DISCORD_TOKEN para ativar o bot.");
            return;
        }

        createLinkIndexes();

        try {
            jda = JDABuilder.createDefault(botToken)
                    .setActivity(Activity.playing(com.minecraft.core.Constant.SERVER_NAME))
                    .setStatus(OnlineStatus.IDLE)
                    .build();

            jda.awaitReady();
            Core.getLogger().info("[Discord] Bot iniciado com sucesso!");

            scheduler = Executors.newSingleThreadScheduledExecutor();
            startLinkCodeCleanup();

        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao iniciar bot: " + e.getMessage());
        }
    }

    private void createLinkIndexes() {
        try {
            MySQLDatabase mysql = Core.getMysql();
            if (mysql != null) {
                try (Connection conn = mysql.getConnection()) {
                    try {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "CREATE INDEX idx_link_code ON link_codes(code)"
                        )) {
                            stmt.execute();
                        }
                    } catch (SQLException ignored) {}

                    try {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "CREATE INDEX idx_link_player_name ON link_codes(player_name)"
                        )) {
                            stmt.execute();
                        }
                    } catch (SQLException ignored) {}

                    Core.getLogger().info("[Discord] Índices da tabela de vínculo verificados.");
                }
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao criar índices: " + e.getMessage());
        }
    }

    public void updateActivity() {
        if (jda == null) return;
        
        int players = ProxyServer.getInstance().getOnlineCount();
        String text = activityText.replace("{players}", String.valueOf(players));
        
        try {
            jda.getPresence().setActivity(Activity.playing(text));
        } catch (Exception e) {
            Core.getLogger().warning("[Discord] Erro ao atualizar atividade: " + e.getMessage());
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public void sendPunishmentEmbed(String playerName, String type, String reason, int punishmentCount, String author, long expiresAt) {
        Core.getLogger().info("[Discord] Tentando enviar embed de punição para " + playerName + " no canal " + punishmentsChannel);
        
        if (jda == null || punishmentsChannel.isEmpty()) {
            Core.getLogger().warning("[Discord] JDA ou canal de punições não disponível");
            return;
        }

        try {
            long channelIdLong = Long.parseLong(punishmentsChannel);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            
            Core.getLogger().info("[Discord] Canal encontrado: " + (channel != null ? "sim" : "não"));
            
            if (channel == null) return;
                
            boolean isBan = type.equalsIgnoreCase("BAN") || type.equalsIgnoreCase("BANIP") || type.equalsIgnoreCase("TEMPBAN");
            Color embedColor = isBan ? new Color(255, 100, 100) : new Color(244, 208, 63);
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail("https://mc-heads.net/avatar/" + playerName)
                    .setColor(embedColor);
            
            if (isBan) {
                // Embed para Banimento
                embed.setAuthor("🔨 BANIDO!")
                        .setTitle(playerName)
                        .addField("📣 Motivo", reason, false);
            } else {
                // Embed para Silenciamento (Mute)
                embed.setAuthor("🔇 SILENCIADO!")
                        .setTitle(playerName)
                        .addField("📣 Motivo", reason, false);
                
                // Adiciona campo de expiração
                String expirationText = "Nunca";
                if (expiresAt != -1) {
                    long remaining = expiresAt - System.currentTimeMillis();
                    if (remaining > 0) {
                        long days = remaining / (24 * 60 * 60 * 1000);
                        long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
                        
                        if (days > 0) {
                            expirationText = days + "d " + hours + "h";
                        } else if (hours > 0) {
                            expirationText = hours + "h " + minutes + "m";
                        } else {
                            expirationText = minutes + "m";
                        }
                    }
                }
                embed.addField("⏰ Expira em", expirationText, false);
            }

            channel.sendMessageEmbeds(embed.build()).queue(
                    message -> {
                        Core.getLogger().info("[Discord] Embed de punição enviada para " + playerName);
                        String randomEmoji = REACTION_EMOJIS[RANDOM.nextInt(REACTION_EMOJIS.length)];
                        message.addReaction(Emoji.fromUnicode(randomEmoji)).queue();
                    },
                    error -> Core.getLogger().severe("[Discord] Erro ao enviar embed: " + error.getMessage())
            );

        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar embed de punição: " + e.getMessage());
        }
    }

    public void sendReportEmbed(String reportedPlayer, String reason, String reporter, boolean online) {
        if (jda == null || reportsChannel.isEmpty()) return;
        
        try {
            long channelIdLong = Long.parseLong(reportsChannel);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            if (channel == null) return;

            Color embedColor = new Color(255, 0, 0);
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor("⚠️ NOVO REPORT", null, "https://cdn.discordapp.com/emojis/warning.png")
                    .setTitle(reportedPlayer)
                    .setDescription("### 📢 Motivo\n" + reason)
                    .addField("Denunciado por", reporter, true)
                    .addField("Status", online ? "🟢 Online" : "🔴 Offline", true)
                    .setThumbnail("https://mc-heads.net/avatar/" + reportedPlayer)
                    .setColor(embedColor);

            channel.sendMessageEmbeds(embed.build()).queue(
                    message -> {
                        Core.getLogger().info("[Discord] Embed de report enviada para " + reportedPlayer);
                        String randomEmoji = REACTION_EMOJIS[RANDOM.nextInt(REACTION_EMOJIS.length)];
                        message.addReaction(Emoji.fromUnicode(randomEmoji)).queue();
                    },
                    error -> Core.getLogger().severe("[Discord] Erro ao enviar embed: " + error.getMessage())
            );

        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar embed de report: " + e.getMessage());
        }
    }

    public void sendRankChangeEmbed(String playerName, String rankName, String rankColor, String author, String action) {
        if (jda == null || ranksChannel.isEmpty()) return;

        try {
            long channelIdLong = Long.parseLong(ranksChannel);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            if (channel == null) return;

            Color embedColor;
            String actionIcon;
            String actionText;

            if (action.equalsIgnoreCase("ADD") || action.equalsIgnoreCase("SET")) {
                embedColor = new Color(100, 255, 100);
                actionIcon = "⬆️ RANK ADICIONADO";
            } else {
                embedColor = new Color(255, 100, 100);
                actionIcon = "⬇️ RANK REMOVIDO";
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(actionIcon, null, "https://cdn.discordapp.com/emojis/" + (action.equalsIgnoreCase("ADD") || action.equalsIgnoreCase("SET") ? "arrow_up.png" : "arrow_down.png"))
                    .setTitle(playerName)
                    .setDescription("### 🎖️ Rank\n" + rankColor + rankName)
                    .addField("👤 Responsável", author, true)
                    .addField("📋 Ação", action.equalsIgnoreCase("ADD") ? "Adicionou" : action.equalsIgnoreCase("SET") ? "Setou" : "Removeu", true)
                    .setThumbnail("https://mc-heads.net/avatar/" + playerName)
                    .setColor(embedColor)
                    .setFooter(com.minecraft.core.Constant.SERVER_NAME, null);

            channel.sendMessageEmbeds(embed.build()).queue(
                    message -> {
                        Core.getLogger().info("[Discord] Embed de rank enviada para " + playerName);
                        String randomEmoji = REACTION_EMOJIS[RANDOM.nextInt(REACTION_EMOJIS.length)];
                        message.addReaction(Emoji.fromUnicode(randomEmoji)).queue();
                    },
                    error -> Core.getLogger().severe("[Discord] Erro ao enviar embed de rank: " + error.getMessage())
            );

        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar embed de rank: " + e.getMessage());
        }
    }

    public void sendStaffChatMessage(String playerName, String message) {
        if (jda == null || staffChatChannel.isEmpty()) return;

        try {
            long channelIdLong = Long.parseLong(staffChatChannel);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            if (channel == null) return;

            // Criar ou obter webhook
            channel.retrieveWebhooks().queue(webhooks -> {
                net.dv8tion.jda.api.entities.Webhook webhook = webhooks.stream()
                        .filter(w -> w.getName().equalsIgnoreCase("staffchat-" + playerName.toLowerCase()))
                        .findFirst()
                        .orElse(null);

                if (webhook == null) {
                    // Criar novo webhook
                    channel.createWebhook("staffchat-" + playerName.toLowerCase()).queue(newWebhook -> {
                        sendWebhookMessage(newWebhook, playerName, message);
                    }, error -> Core.getLogger().severe("[Discord] Erro ao criar webhook: " + error.getMessage()));
                } else {
                    sendWebhookMessage(webhook, playerName, message);
                }
            });

        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar staffchat: " + e.getMessage());
        }
    }

    private void sendWebhookMessage(net.dv8tion.jda.api.entities.Webhook webhook, String playerName, String message) {
        try {
            webhook.sendMessage(message)
                    .setUsername(playerName)
                    .setAvatarUrl("https://mc-heads.net/avatar/" + playerName)
                    .queue(
                            success -> Core.getLogger().info("[Discord] Staffchat enviado: " + playerName),
                            error -> Core.getLogger().severe("[Discord] Erro ao enviar via webhook: " + error.getMessage())
                    );
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar staffchat: " + e.getMessage());
        }
    }
    
    public void sendLobbyChatMessage(String serverName, String playerName, String message, String rankColor) {
        if (jda == null) return;
        
        String channelId = lobbyChatChannels.get(serverName.toUpperCase());
        if (channelId == null) return;
        
        try {
            long channelIdLong = Long.parseLong(channelId);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            if (channel == null) return;
            
            channel.retrieveWebhooks().queue(webhooks -> {
                net.dv8tion.jda.api.entities.Webhook webhook = webhooks.stream()
                        .filter(w -> w.getName().equalsIgnoreCase("chat-" + serverName.toLowerCase()))
                        .findFirst()
                        .orElse(null);
                
                if (webhook == null) {
                    channel.createWebhook("chat-" + serverName.toLowerCase()).queue(newWebhook -> {
                        sendLobbyChatWebhook(newWebhook, playerName, message, rankColor);
                    }, error -> Core.getLogger().severe("[Discord] Erro ao criar webhook: " + error.getMessage()));
                } else {
                    sendLobbyChatWebhook(webhook, playerName, message, rankColor);
                }
            });
            
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar chat: " + e.getMessage());
        }
    }
    
    private void sendLobbyChatWebhook(net.dv8tion.jda.api.entities.Webhook webhook, String playerName, String message, String rankColor) {
        try {
            webhook.sendMessage(message)
                    .setUsername(playerName)
                    .setAvatarUrl("https://mc-heads.net/avatar/" + playerName)
                    .queue();
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao enviar chat: " + e.getMessage());
        }
    }
    
    public void sendShopOrderEmbed(String playerName, String email, String productName, double amount, String orderId) {
        if (jda == null || shopOrdersChannel.isEmpty()) return;
        
        try {
            long channelIdLong = Long.parseLong(shopOrdersChannel);
            TextChannel channel = jda.getTextChannelById(channelIdLong);
            if (channel == null) return;
            
            String message = "**§a§lNOVO PEDIDO #" + orderId + "**\n\n" +
                    "**§eJogador:** " + playerName + "\n" +
                    "**§eEmail:** " + email + "\n" +
                    "**§eProduto:** " + productName + "\n" +
                    "**§eValor:** R$" + String.format("%.2f", amount) + "\n\n" +
                    "**§eAções:**\n" +
                    "✅ Aceitar: `accept " + orderId + "`\n" +
                    "❌ Rejeitar: `reject " + orderId + "`";
            
            channel.sendMessage(message)
                    .queue(
                            msg -> Core.getLogger().info("[Shop] Pedido #" + orderId + " enviado para " + playerName),
                            error -> Core.getLogger().severe("[Shop] Erro ao enviar pedido: " + error.getMessage())
                    );
            
        } catch (Exception e) {
            Core.getLogger().severe("[Shop] Erro ao enviar pedido: " + e.getMessage());
        }
    }

    public String generateLinkCode(String playerName) {
        String code = StringUtil.generateLetterCode(6);
        long createdAt = System.currentTimeMillis();
        long expiresAt = createdAt + (15 * 60 * 1000);

        try {
            MySQLDatabase mysql = Core.getMysql();
            if (mysql != null) {
                try (Connection conn = mysql.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO link_codes (code, player_id, player_name, created_at, expires_at, used) VALUES (?, ?, ?, ?, ?, 0)")) {
                        ps.setString(1, code);
                        ps.setString(2, UUID.randomUUID().toString());
                        ps.setString(3, playerName);
                        ps.setLong(4, createdAt);
                        ps.setLong(5, expiresAt);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao salvar código no MySQL: " + e.getMessage());
        }

        return code;
    }

    public boolean isLinked(String playerName) {
        try {
            MySQLDatabase mysql = Core.getMysql();
            if (mysql != null) {
                try (Connection conn = mysql.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT linked_discord_id FROM link_codes WHERE player_name = ? AND linked_discord_id IS NOT NULL LIMIT 1")) {
                        ps.setString(1, playerName);
                        try (ResultSet rs = ps.executeQuery()) {
                            return rs.next();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao verificar vínculo: " + e.getMessage());
        }
        return false;
    }

    public String useLinkCode(String code, String discordUserId, String discordUsername) {
        String playerName = null;

        try {
            MySQLDatabase mysql = Core.getMysql();
            if (mysql != null) {
                try (Connection conn = mysql.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT player_name, linked_discord_id FROM link_codes WHERE code = ? AND used = 0 AND expires_at > ?")) {
                        ps.setString(1, code.toUpperCase());
                        ps.setLong(2, System.currentTimeMillis());

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                playerName = rs.getString("player_name");

                                if (rs.getString("linked_discord_id") != null) {
                                    return "§cEsta conta já está vinculada a outro Discord!";
                                }

                                try (PreparedStatement psUpdate = conn.prepareStatement(
                                        "UPDATE link_codes SET used = 1, used_at = ?, linked_discord_id = ?, linked_discord_name = ? WHERE code = ?")) {
                                    psUpdate.setLong(1, System.currentTimeMillis());
                                    psUpdate.setString(2, discordUserId);
                                    psUpdate.setString(3, discordUsername);
                                    psUpdate.setString(4, code.toUpperCase());
                                    psUpdate.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao usar código do MySQL: " + e.getMessage());
        }

        return playerName;
    }

    public boolean unlinkAccount(String playerName) {
        try {
            MySQLDatabase mysql = Core.getMysql();
            if (mysql != null) {
                try (Connection conn = mysql.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE link_codes SET linked_discord_id = NULL, linked_discord_name = NULL WHERE player_name = ? AND linked_discord_id IS NOT NULL")) {
                        ps.setString(1, playerName);
                        int updated = ps.executeUpdate();
                        return updated > 0;
                    }
                }
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Discord] Erro ao desvincular conta: " + e.getMessage());
        }
        return false;
    }

    private void startLinkCodeCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                MySQLDatabase mysql = Core.getMysql();
                if (mysql != null) {
                    try (Connection conn = mysql.getConnection()) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "DELETE FROM link_codes WHERE expires_at < ? AND used = 0")) {
                            ps.setLong(1, System.currentTimeMillis());
                            ps.executeUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                Core.getLogger().severe("[Discord] Erro ao limpar códigos expirados: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}