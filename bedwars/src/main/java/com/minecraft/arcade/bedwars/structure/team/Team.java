package com.minecraft.arcade.bedwars.structure.team;

import com.google.gson.JsonObject;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.team.objects.forge.Forge;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.TeamUpgrade;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.armor.ArmorProtection;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.haste.Haste;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.sword.Sword;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.Trap;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.map.location.SyntheticLocation;
import com.minecraft.core.arcade.room.team.objects.IslandArea;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Team extends TeamPreset {

    private final Forge forge;
    private final IslandArea area;

    private final Map<UUID, Long> invaders;

    private TeamUpgrade upgrade;

    private long bedExpiresAt = -1L;

    public Team(TeamPresetType type, Arena arena, Location base, int maxPlayers) {
        super(type, arena, base, maxPlayers);

        this.forge = new Forge(arena, this);

        this.invaders = new ConcurrentHashMap<>();
        this.upgrade = new TeamUpgrade();

        JsonObject json = arena.getMap().getConfig().get("island_area").getAsJsonObject();

        IslandArea area = new IslandArea(
                json.has("frontAndBack") ? json.get("frontAndBack").getAsInt() : 0,
                json.has("up") ? json.get("up").getAsInt() : 0,
                json.has("sides") ? json.get("sides").getAsInt() : 0
        );

        SyntheticLocation synthetic = arena.getSynthetic(type.name().toLowerCase() + "_spawn");

        area.setAxisX(synthetic != null && synthetic.isAxisX());

        this.area = area;
    }

    @Override
    public boolean isDead() {
        return !hasBed() && getAliveUsers().isEmpty();
    }

    @Override
    public String getBedName(int aliveUsers) {
        return hasBed() ? (bedExpiresAt > -1L ? "§e" : "§a") + "✓" : aliveUsers > 0 ? "§a" + aliveUsers : "§c✘";
    }

    public List<User> getAliveUsers() {
        return ((Arena) getArena()).getAliveUsers(this);
    }

    public boolean inIsland(Player player) {
        return inIsland(player.getLocation());
    }

    public boolean inIsland(Location location) {
        return Cuboid.isProtectedArea(getBase(), location, area.getFrontAndBack(), area.getUp(), area.getSides(), area.isAxisX());
    }

    public boolean hasExpired() {
        long currentTime = System.currentTimeMillis();
        return bedExpiresAt > -1L && bedExpiresAt <= currentTime;
    }

    public void destructBed() {
        if (hasBed())
            getBedLocation().getBlock().setType(Material.AIR);

        setBedExpiresAt(-1L);
    }

    /* Trap System */
    public Queue<Trap> getTraps() {
        return upgrade.getTraps();
    }

    public boolean isInvader(UUID id) {
        if (!invaders.containsKey(id)) return false;

        if (invaders.get(id) <= System.currentTimeMillis()) {
            invaders.remove(id);
            return false;
        }

        return true;
    }

    public void setInvader(Player player) {
        if (isInvader(player.getUniqueId())) return;

        invaders.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));

        if (!getTraps().isEmpty()) {
            Trap trap = getTraps().poll();

            if (trap != null) {
                trap.execute(player, this);
                getPlayers().forEach(member -> member.sendMessage("§e" + player.getName() + "§c invadiu a sua ilha!"));
                player.sendTitle("§c§lARMADILHA ATIVADA!", "§eEle(a) sabe da sua presença...");
            }
        }
    }

    public int getTotalInvaders() {
        long currentTime = System.currentTimeMillis();

        invaders.entrySet().removeIf(entry -> entry.getValue() <= currentTime);

        return invaders.size();
    }

    /* Upgrade */
    public Sword getSword() {
        return upgrade.getSword();
    }

    public Sword getNextSword() {
        return !hasSword() ? Sword.ONE : Sword.values()[getSword().ordinal() + 1];
    }

    public boolean isHighestSword() {
        return hasSword() && getSword().isHighest();
    }

    public boolean canBuySword(Player player) {
        if (isHighestSword()) return false;

        return BukkitUtil.getItemAmount(player, Material.DIAMOND) >= getNextSword().getAdjustedPrice((Arena) getArena());
    }

    public boolean hasSword() {
        return getSword() != null;
    }

    public Haste getHaste() {
        return upgrade.getHaste();
    }

    public Haste getNextHaste() {
        return !hasHaste() ? Haste.ONE : Haste.values()[getHaste().ordinal() + 1];
    }

    public boolean isHighestHaste() {
        return hasHaste() && getHaste().isHighest();
    }

    public boolean canBuyHaste(Player player) {
        if (isHighestHaste()) return false;

        return BukkitUtil.getItemAmount(player, Material.DIAMOND) >= getNextHaste().getAdjustedPrice((Arena) getArena());
    }

    public boolean hasHaste() {
        return getHaste() != null;
    }

    public ArmorProtection getArmor() {
        return upgrade.getArmor();
    }

    public ArmorProtection getNextArmor() {
        return !hasArmor() ? ArmorProtection.ONE : ArmorProtection.values()[getArmor().ordinal() + 1];
    }

    public boolean isHighestArmor() {
        return hasArmor() && getArmor().isHighest();
    }

    public boolean canBuyArmor(Player player) {
        if (isHighestArmor()) return false;

        return BukkitUtil.getItemAmount(player, Material.DIAMOND) >= getNextArmor().getAdjustedPrice((Arena) getArena());
    }

    public boolean hasArmor() {
        return getArmor() != null;
    }
}
