package com.minecraft.core.bukkit.api.protocol;

import com.minecraft.core.Core;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.minecraft.core.bukkit.user.UserModel;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.*;

public class ProtocolHandler {

    public static void changePlayerName(Player player, String name) {
        changePlayerName(player, name, true);
    }

    public static void changePlayerName(Player player, String name, boolean respawn) {
        try {
            Object minecraftServer = MinecraftReflection.getMinecraftServerClass().getMethod("getServer").invoke(null);
            Object playerList = minecraftServer.getClass().getMethod("getPlayerList").invoke(minecraftServer);

            Field f = playerList.getClass().getSuperclass().getDeclaredField("playersByName");
            f.setAccessible(true);

            Map<String, Object> playersByName = (Map<String, Object>) f.get(playerList);
            playersByName.remove(player.getName());

            WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);

            Field field = profile.getHandle().getClass().getDeclaredField("name");

            field.setAccessible(true);
            field.set(profile.getHandle(), name);
            field.setAccessible(false);

            playersByName.put(name, MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player));
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (respawn)
            respawn(player);
    }

    public static void handleTeleport(Location location) {
        Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());

        Iterator<Player> iterator = players.iterator();

        // Teleport Scheduler
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    Player player = iterator.next();

                    if (player != null) {
                        iterator.remove();

                        player.teleport(location);
                    }
                } else
                    cancel();
            }
        }.runTaskTimer(BukkitCore.getInstance(), 0, 3);
    }

    public static void sendBar(Player player, String text) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
        packet.getBytes().write(0, (byte) 2);

        if (player == null || text.isEmpty() || BukkitCore.getManager().getProtocol() == null) {
            return;
        }

        try {
            BukkitCore.getManager().getProtocol().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openBook(Player player, ItemStack book) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);

        player.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte) 0);
        buf.writerIndex(1);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        player.getInventory().setItem(slot, old);
    }

    private static void respawn(Player player) {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        double x = entity.locX;
        double y = entity.locY;
        double z = entity.locZ;

        WorldServer worldserver = (WorldServer) entity.getWorld();
        DedicatedPlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();

        PacketPlayOutEntityDestroy destroyEntity = new PacketPlayOutEntityDestroy(entity.getId());
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity);
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);
        PacketPlayOutNamedEntitySpawn spawnEntity = new PacketPlayOutNamedEntitySpawn(entity);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
        PacketPlayOutHeldItemSlot heldItemSlot = new PacketPlayOutHeldItemSlot(entity.inventory.itemInHandIndex);
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(entity, (byte) 28);
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(worldserver.worldProvider.getDimension(), worldserver.getDifficulty(), worldserver.getWorldData().getType(), entity.playerInteractManager.getGameMode());
        PacketPlayOutPosition position = new PacketPlayOutPosition(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch, Collections.emptySet());
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entity, (byte) MathHelper.d(entity.getHeadRotation() * 256.0F / 360.0F));

        Core.getPlatform().runSync(() -> {
            for (int i = 0; i < playerList.players.size(); i++) {
                EntityPlayer ep1 = playerList.players.get(i);

                if (ep1.getBukkitEntity().canSee(entity.getBukkitEntity())) {
                    PlayerConnection playerConnection = ep1.playerConnection;
                    playerConnection.sendPacket(removePlayer);
                    playerConnection.sendPacket(addPlayer);

                    if (ep1.getId() != entity.getId()) {
                        playerConnection.sendPacket(destroyEntity);
                        playerConnection.sendPacket(spawnEntity);
                    }

                    playerConnection.sendPacket(headRotation);
                }
            }

            PlayerConnection con = entity.playerConnection;
            con.sendPacket(metadata);
            con.sendPacket(respawn);
            con.sendPacket(position);
            con.sendPacket(heldItemSlot);
            con.sendPacket(status);
            entity.updateAbilities();
            entity.triggerHealthUpdate();
            entity.updateInventory(entity.activeContainer);
            entity.updateInventory(entity.defaultContainer);
        });

        CraftPlayer craftPlayer = entity.getBukkitEntity();

        craftPlayer.getInventory().setArmorContents(craftPlayer.getInventory().getArmorContents());
        craftPlayer.setExp(craftPlayer.getExp());
        craftPlayer.setHealth(craftPlayer.getHealth());
        craftPlayer.setSneaking(craftPlayer.isSneaking());

        if (craftPlayer.getPassenger() != null)
            craftPlayer.setPassenger(craftPlayer.getPassenger());

        if (craftPlayer.isInsideVehicle())
            craftPlayer.getVehicle().setPassenger(craftPlayer);

        entity.locX = x;
        entity.locY = y;
        entity.locZ = z;
        entity.lastX = x;
        entity.lastY = y;
        entity.lastZ = z;

        entity.setPosition(x, y, z);
    }

    public static void respawn(Player player, Player target, WrappedGameProfile gameProfile) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        // Pacote de remoção com o GameProfile temporário
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);

        // Pacote de adição com o GameProfile temporário
        // Usamos reflexão para substituir o GameProfile real pelo temporário
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);

        try {
            Field profileField = addPlayer.getClass().getDeclaredField("b"); // Campo interno que armazena o GameProfile
            profileField.setAccessible(true);

            List<PacketPlayOutPlayerInfo.PlayerInfoData> playerInfoDataList = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) profileField.get(addPlayer);

            for (PacketPlayOutPlayerInfo.PlayerInfoData data : playerInfoDataList) {
                if (data.a().getId().equals(player.getUniqueId())) {
                    PacketPlayOutPlayerInfo.PlayerInfoData infoData = addPlayer.new PlayerInfoData(
                            (GameProfile) gameProfile.getHandle(), data.b(), data.c(), data.d());

                    playerInfoDataList.set(playerInfoDataList.indexOf(data), infoData);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pacotes de entidade
        PacketPlayOutEntityDestroy destroyEntity = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        PacketPlayOutNamedEntitySpawn spawnEntity = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) MathHelper.d(entityPlayer.getHeadRotation() * 256.0F / 360.0F));

        Core.getPlatform().runSync(() -> {
            EntityPlayer targetEntity = ((CraftPlayer) target).getHandle();

            PlayerConnection connection = targetEntity.playerConnection;

            if (targetEntity.getBukkitEntity().canSee(entityPlayer.getBukkitEntity())) {
                connection.sendPacket(removePlayer);
                connection.sendPacket(addPlayer);
                connection.sendPacket(destroyEntity);
                connection.sendPacket(spawnEntity);
                connection.sendPacket(headRotation);
            }
        });
    }

    public static void changePlayerSkin(Player player, Skin skin) {
        changePlayerSkin(player, skin, true);
    }

    public static void changePlayerSkin(Player player, Skin skin, Player target) {
        changePlayerSkin(player, skin, target, true);
    }

    public static void changePlayerSkin(Player player, Skin skin, Player target, boolean respawn) {
        WrappedGameProfile skinProfile = new WrappedGameProfile(player.getUniqueId(), player.getName());

        skinProfile.getProperties().clear();

        skinProfile.getProperties().put("textures", new WrappedSignedProperty("textures",
                skin.getValue(),
                skin.getSignature()));

        if (respawn)
            respawn(player, target, skinProfile);
    }

    private static void handleGameProfile(Player player, Skin skin) {
        WrappedGameProfile playerProfile = WrappedGameProfile.fromPlayer(player);

        WrappedGameProfile skinProfile = new WrappedGameProfile(skin.getId(), skin.getDisplayName());

        skinProfile.getProperties().clear();

        skinProfile.getProperties().put("textures", new WrappedSignedProperty("textures",
                skin.getValue(),
                skin.getSignature()));

        WrappedSignedProperty property = skinProfile.getProperties().get("textures").stream().findFirst().orElse(null);

        playerProfile.getProperties().clear();
        playerProfile.getProperties().put("textures", property);
    }

    public static void changePlayerSkin(Player player, Skin skin, boolean respawn) {
        if (skin == null || !skin.isValid()) {
            return;
        }

        handleGameProfile(player, skin);

        /* Atualizar Cabeça */
        PlayerInventory inv = player.getInventory();

        if (!Core.getServerType().isArcade()) {
            if (inv.contains(Material.SKULL_ITEM)) {

                for (ItemStack item : inv.getContents()) {
                    if (item == null || !item.getType().equals(Material.SKULL_ITEM)) continue;

                    SkullMeta meta = (SkullMeta) item.getItemMeta();

                    if (skin.getValue() == null || skin.getValue().isEmpty()) {
                        continue;
                    }

                    GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                    profile.getProperties().put("textures", new Property("textures", skin.getValue()));

                    Field field;
                    try {
                        field = meta.getClass().getDeclaredField("profile");
                        field.setAccessible(true);
                        field.set(meta, profile);
                    } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                        Core.getLogger().warning("Erro ao atualizar textura de skull: " + e.getMessage());
                    }

                    item.setItemMeta(meta);
                }
            }

            /* Atualizar NPC com a mesma Skin */
            for (NpcClient client : BukkitCore.getManager().getNpc().getClients(player)) {
                if (client == null || !client.isSpawned() || client.isSameTexture(skin)) continue;

                client.updateTexture(new Property("textures", skin.getValue(), skin.getSignature()));

                Core.getPlatform().runAsync(client::respawn);
            }
        }

        if (respawn)
            respawn(player);
    }

    public static void removePlayerSkin(Player player) {
        removePlayerSkin(player, true);
    }

    public static void removePlayerSkin(Player player, boolean respawn) {
        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
        profile.getProperties().clear();

        if (respawn) {
            respawn(player);
        }
    }

    public static void hideArmor(Player player, Player receiver) {
        if (player.equals(receiver)) return;

        int id = player.getEntityId();

        PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;

        for (int i = 1; i <= 4; i++) {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(id, i, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));

            connection.sendPacket(packet);
        }
    }

    public static void showArmor(Player player, Player receiver) {
        if (player.equals(receiver)) return;

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        EntityPlayer boundTo = ((CraftPlayer) receiver).getHandle();

        for (int i = 0; i < 4; i++) {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                    entityPlayer.getId(),
                    4 - i,
                    entityPlayer.inventory.getArmorContents()[3 - i]
            );

            boundTo.playerConnection.sendPacket(packet);
        }
    }

    public static void hidePlayerName(Player player, Player receiver) {
        if (player.equals(receiver)) return;

        UserModel user = UserModel.of(player.getUniqueId());

        if (user == null) return;

        String order = "tag:" + user.getTag().getOrder() + ":" + player.getEntityId();

        org.bukkit.scoreboard.Team team = getTagTeam(receiver, order);

        if (team != null && !team.getNameTagVisibility().equals(NameTagVisibility.NEVER)) {
            team.setNameTagVisibility(NameTagVisibility.NEVER);

            receiver.setScoreboard(team.getScoreboard());
        }
    }

    public static void showPlayerName(Player player, Player receiver) {
        if (player.equals(receiver)) return;

        UserModel user = UserModel.of(player.getUniqueId());

        if (user == null) return;

        String order = "tag:" + user.getTag().getOrder() + ":" + player.getEntityId();

        org.bukkit.scoreboard.Team team = getTagTeam(receiver, order);

        if (team != null && team.getNameTagVisibility().equals(NameTagVisibility.NEVER)) {
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);

            receiver.setScoreboard(team.getScoreboard());
        }
    }

    public static org.bukkit.scoreboard.Team getTagTeam(Player player, String order) {
        Scoreboard scoreboard = player.getScoreboard();

        return scoreboard.getTeam(order);
    }
}
