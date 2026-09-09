package com.minecraft.core.bukkit.api.npc;

import com.minecraft.core.Core;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.core.bukkit.api.npc.facing.NpcFacing;
import com.minecraft.core.bukkit.manager.list.NpcManager;
import com.minecraft.core.util.list.ReflectionUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Npc {

    private static int npcCount = 0;

    protected final NpcManager npcManager = BukkitCore.getManager().getNpc();

    private final int npcId;

    private final UUID uuid = UUID.randomUUID();
    private final GameProfile profile;

    private NpcFacing facing = NpcFacing.NONE;

    private Location location;

    private String tag;
    private ItemStack hand, helmet, chestPlate, leggings, boots;

    private NpcAction action;
    private final Set<Player> viewers = new HashSet<>();

    private int entityId, batEntityId;
    private boolean spawned, contact;

    public Npc(@NonNull Location location) {
        this.npcId = npcCount++;
        this.location = location;

        this.profile = new GameProfile(uuid, "§8NPC_" + uuid.toString().substring(0, 8));
    }

    public World getWorld() {
        return location.getWorld();
    }

    public List<Player> getPlayers() {
        return location.getWorld().getPlayers();
    }

    public double getEyeHeight() {
        return 1.62D;
    }

    public void display() {
        if (isSpawned()) return;

        setSpawned(true);

        setEntityId(npcManager.nextEntityId());
        setBatEntityId(npcManager.nextEntityId());

        npcManager.add(this);
    }

    public void setFacing(NpcFacing facing) {
        Location clone = location.clone();

        float yaw = clone.getYaw();

        switch (facing) {
            case FRONT:
                if (yaw < 0) {
                    yaw = -yaw; // convert negative yaw to positive
                }
                yaw = normalizeYaw(yaw);
                break;
            case LEFT_SIDE:
                if (yaw < 0) {
                    yaw = -yaw - 90; // convert negative yaw to positive and subtract 90
                } else {
                    yaw = yaw - 90;
                }
                yaw = normalizeYaw(yaw);
                break;
            case RIGHT_SIDE:
                if (yaw < 0) {
                    yaw = -yaw + 90; // convert negative yaw to positive and add 90
                } else {
                    yaw = yaw + 90;
                }
                yaw = normalizeYaw(yaw);
                break;
        }

        clone.setYaw(yaw);

        this.facing = facing;
        this.location = clone;
    }

    private float normalizeYaw(float yaw) {
        if (yaw < -180) {
            yaw += 360;
        } else if (yaw > 180) {
            yaw -= 360;
        }
        return yaw;
    }

    public void destroy() {
        if (!isSpawned()) return;

        setSpawned(false);

        npcManager.remove(this);
    }

    public void respawnTo(Player player) {
        despawnTo(player);
        spawnTo(player);
    }

    public void spawnTo(Player player) {
        if (player == null || !player.isOnline() || getViewers().contains(player)) return;

        getViewers().add(player);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(buildPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, profile));
        connection.sendPacket(buildSpawnHumanPacket(entityId, location, profile));
        connection.sendPacket(buildEntityHeadRotationPacket(entityId, location.getYaw()));
        connection.sendPacket(buildSpawnBatPacket(batEntityId, location));
        connection.sendPacket(buildAttachPacket(batEntityId, entityId));

        updateEquipment(player);

        Core.getPlatform().runSync(() -> connection.sendPacket(buildPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, profile)), 40);
    }

    public void despawnTo(Player player) {
        if (player == null || !player.isOnline() || !getViewers().contains(player)) return;

        getViewers().remove(player);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(buildPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, profile));
        connection.sendPacket(buildDestroyPacket(entityId, batEntityId));
    }

    public void releaseContact() {
        this.contact = true;
    }

    public void updateEquipment() {
        getViewers().forEach(this::updateEquipment);
    }

    private void updateEquipment(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(buildEquipmentPacket(entityId, 0, hand));
        connection.sendPacket(buildEquipmentPacket(entityId, 1, boots));
        connection.sendPacket(buildEquipmentPacket(entityId, 2, leggings));
        connection.sendPacket(buildEquipmentPacket(entityId, 3, chestPlate));
        connection.sendPacket(buildEquipmentPacket(entityId, 4, helmet));
    }

    public void updateTexture(Property textures) {
        if (textures == null || textures.getValue() == null || textures.getValue().isEmpty()) {
            Core.getLogger().warning("Tentativa de atualizar NPC com textura nula ou vazia");
            return;
        }

        try {
            String decodedValue = new String(java.util.Base64.getDecoder().decode(textures.getValue()), java.nio.charset.StandardCharsets.UTF_8);
            com.google.gson.JsonObject textureJson = Core.GSON.fromJson(decodedValue, com.google.gson.JsonObject.class);
            
            if (textureJson == null || !textureJson.has("textures") || 
                !textureJson.getAsJsonObject("textures").has("SKIN") ||
                !textureJson.getAsJsonObject("textures").getAsJsonObject("SKIN").has("url") ||
                textureJson.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString().isEmpty()) {
                Core.getLogger().warning("Textura do NPC contém dados inválidos ou incompletos");
                return;
            }
        } catch (Exception e) {
            Core.getLogger().warning("Erro ao validar textura do NPC: " + e.getMessage());
            return;
        }

        GameProfile skinProfile = new GameProfile(getUuid(), "§8NPC_" + getUuid().toString().substring(0, 8));

        skinProfile.getProperties().clear();
        skinProfile.getProperties().put("textures", textures);

        Property skinTextures = skinProfile.getProperties().get("textures").stream().findFirst().orElse(null);

        if (skinTextures != null) {
            getProfile().getProperties().clear();

            getProfile().getProperties().put("textures", skinTextures);
        }

        Set<Player> viewers = new HashSet<>(getViewers());

        viewers.forEach(this::respawnTo);
    }

    public boolean isSameTexture(Skin skin) {
        PropertyMap map = profile.getProperties();

        if (map == null || !map.containsKey("textures")) return false;

        Property textures = map.get("textures").stream().findAny().orElse(null);

        if (textures == null) return false;

        return textures.getValue().equalsIgnoreCase(skin.getValue()) && textures.getSignature().equalsIgnoreCase(skin.getSignature());
    }

    private static PacketPlayOutNamedEntitySpawn buildSpawnHumanPacket(int entityId, Location loc, GameProfile profile) {
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(10, (byte) 127); // (0b01111111) ativando todas as partes da skin para 1.8+
        dataWatcher.a(6, (float) 20);

        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
        ReflectionUtil.setValue(packet, "a", entityId);//id
        ReflectionUtil.setValue(packet, "b", profile.getId());//uuid
        ReflectionUtil.setValue(packet, "c", floor(loc.getX() * 32));//x
        ReflectionUtil.setValue(packet, "d", floor(loc.getY() * 32));//y
        ReflectionUtil.setValue(packet, "e", floor(loc.getZ() * 32));//z
        ReflectionUtil.setValue(packet, "f", getCompressedAngle(loc.getYaw()));//yaw
        ReflectionUtil.setValue(packet, "g", getCompressedAngle(loc.getPitch()));//pitch
        ReflectionUtil.setValue(packet, "h", 0); // item hand
        ReflectionUtil.setValue(packet, "i", dataWatcher);

        return packet;
    }

    private static PacketPlayOutEntityHeadRotation buildEntityHeadRotationPacket(int entityId, float yaw) {
        PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation();
        ReflectionUtil.setValue(packet, "a", entityId);
        ReflectionUtil.setValue(packet, "b", getCompressedAngle(yaw));
        return packet;
    }

    private static PacketPlayOutPlayerInfo buildPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile profile) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        ReflectionUtil.setValue(packet, "a", action);

        List<PacketPlayOutPlayerInfo.PlayerInfoData> dataList = ReflectionUtil.getValue(packet, "b");
        IChatBaseComponent nameComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + profile.getName() + "\"}");
        dataList.add(packet.new PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.NOT_SET, nameComponent));

        return packet;
    }

    private static PacketPlayOutSpawnEntityLiving buildSpawnBatPacket(int entityId, Location loc) {
        DataWatcher watcher = new DataWatcher(null);
        watcher.a(0, (byte) (1 << 5));

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
        ReflectionUtil.setValue(packet, "a", entityId);
        ReflectionUtil.setValue(packet, "b", 65);
        ReflectionUtil.setValue(packet, "c", floor(loc.getX() * 32D));
        ReflectionUtil.setValue(packet, "d", floor(loc.getY() * 32D));
        ReflectionUtil.setValue(packet, "e", floor(loc.getZ() * 32D));
        ReflectionUtil.setValue(packet, "l", watcher);

        return packet;
    }

    private static PacketPlayOutAttachEntity buildAttachPacket(int a, int b) {
        PacketPlayOutAttachEntity packet = new PacketPlayOutAttachEntity();
        ReflectionUtil.setValue(packet, "a", 0);
        ReflectionUtil.setValue(packet, "b", a);
        ReflectionUtil.setValue(packet, "c", b);
        return packet;
    }

    private static PacketPlayOutEntityDestroy buildDestroyPacket(int... entityIds) {
        return new PacketPlayOutEntityDestroy(entityIds);
    }

    private static PacketPlayOutEntityEquipment buildEquipmentPacket(int entityId, int slot, ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        return new PacketPlayOutEntityEquipment(entityId, slot, CraftItemStack.asNMSCopy(item));
    }

    private static int floor(double var0) {
        int var2 = (int) var0;
        return var0 < (double) var2 ? var2 - 1 : var2;
    }

    protected static byte getCompressedAngle(float value) {
        return (byte) ((int) value * 256.0F / 360.0F);
    }
}