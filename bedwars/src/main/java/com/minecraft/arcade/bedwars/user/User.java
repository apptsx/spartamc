package com.minecraft.arcade.bedwars.user;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.member.list.bedwars.objects.ability.Ability;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.context.UserContext;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class User extends UserModel {

    private final BedMember member;
    private UserContext context;

    private Arena arena;
    private Team team;

    private int respawnTime;

    public User(BedMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;

        this.context = new UserContext();
        loadSelectedAbilities();
    }

    @Override
    public void handle() {
        Player player = getAccount().player();
        setSidebar(new Sidebar(player, "§6§lBED WARS"));

        if (arena != null) {
            arena.join(player);
            
            Core.getPlatform().runSync(this::applyLoadedAbilities, 10L);
        }
    }

    private void applyLoadedAbilities() {
        Player player = getPlayer();
        if (player == null || !player.isOnline()) return;
    }

    private void loadSelectedAbilities() {
        if (member == null) return;
        
        for (String abilityName : member.getSelectedAbilities()) {
            try {
                AbilityType type = AbilityType.valueOf(abilityName.toUpperCase());
                context.addAbility(new Ability(type));
            } catch (IllegalArgumentException e) {
                Core.getLogger().warning("[User] Habilidade inválida encontrada: " + abilityName);
            }
        }
    }

    /**
     * Ativa as habilidades selecionadas quando a partida começa
     */
    public void activateAbilitiesOnMatchStart() {
        long matchStartTime = System.currentTimeMillis();
        Player player = getPlayer();
        
        if (player == null || !player.isOnline()) return;
        
        for (Ability ability : context.getAbilities()) {
            ability.activate(matchStartTime);
            AbilityType type = ability.getType();
            player.sendMessage("");
            player.sendMessage("§aSua habilidade §6" + type.getName() + " §afoi ativada.");
            
            // Inicializar estado de rastreamento para NO_FALL
            if (type == AbilityType.NO_FALL) {
                context.setNoFallWasActive(true);
            }
        }
    }

    public void reset() {
        setContext(new UserContext());
    }

    public Player getPlayer() {
        return getAccount().player();
    }

    public Arcade getArcade() {
        return arena != null ? arena.getArcade() : null;
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);

        if (state == ArcadeState.RESPAWN) {
            boolean hasQuickRespawn = context.hasAbility(AbilityType.QUICK_RESPAWN);
            this.respawnTime = hasQuickRespawn ? 3 : 5;
        }

        Player player = getPlayer();
        if (player == null) return;

        Core.getPlatform().runSync(() -> {
            switch (state) {
                case RESPAWN: {
                    player.getInventory().clear();
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());

                    player.setFlying(true);

                    player.addPotionEffects(Arrays.asList(
                            new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3)
                    ));

                    player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                    break;
                }

                case DEAD: {
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(true);

                    player.addPotionEffects(Arrays.asList(
                            new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3)
                    ));

                    player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                    break;
                }

                default: {
                    context.setProtectionTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1) + TimeUnit.MILLISECONDS.toMillis(5));

                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(false);
                    break;
                }
            }
        });
    }

    public boolean isProtected() {
        if (!isPlayer() || !inState(ArcadeState.ALIVE)) return true;

        if (context.getProtectionTime() > -1L && context.getProtectionTime() > System.currentTimeMillis()) return true;

        return arena == null || !arena.isPhase(RoomPhase.PLAYING);
    }

    public boolean isImmuneTraps() {
        return context.getMilkTime() > System.currentTimeMillis();
    }

    public int getXpGain() {
        return context.getXpGain();
    }

    public void setXpGain(int xpGain) {
        context.setXpGain(getXpGain() + xpGain);
    }

    /* Stats */
    public int getKills() {
        return context.getKills();
    }

    public int getDeaths() {
        return context.getDeaths();
    }

    public int getFinalKill() {
        return context.getFinalKill();
    }

    public int getBedDestruction() {
        return context.getBedDestruction();
    }

    public void setKills() {
        context.setKills(getKills() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setKills();

            member.updateStats(stats);
        }
        
        // Ganhar moedas por kill
        int coinGain = 10;
        addCoins(coinGain);
    }

    public void setDeaths() {
        context.setDeaths(getDeaths() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setDeaths();

            member.updateStats(stats);
        }
    }

    public void setMatches() {
        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setMatches();

            member.updateStats(stats);
        }
    }

    public void setFinalKill() {
        context.setFinalKill(getFinalKill() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setFinalKill();

            member.updateStats(stats);
        }
        
        int coinGain = 25;
        addCoins(coinGain);
    }

    public void setBedDestruction() {
        context.setBedDestruction(getBedDestruction() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setBedDestruction();

            member.updateStats(stats);
        }
        
        // Ganhar moedas por quebrar cama
        int coinGain = 50;
        addCoins(coinGain);
    }

    public boolean canBuyItem(BedWarsItem item) {
        return BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial()) >= item.getPrice();
    }

    public int getRemainingOre(BedWarsItem item) {
        return item.getPrice() - BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial());
    }

    public void removeOre(BedWarsItem item) {
        BukkitUtil.removeItemByAmount(getPlayer(), item.getOre().getMaterial(), item.getPrice());
    }

    public int getRemainingOre(BedOre ore, int price) {
        return price - BukkitUtil.getItemAmount(getPlayer(), ore.getMaterial());
    }

    public void removeOre(BedOre ore, int price) {
        BukkitUtil.removeItemByAmount(getPlayer(), ore.getMaterial(), price);
    }

    public boolean hasTool(BedWarsItem item) {
        if (!item.isType(BedShop.TOOLS)) return false;

        BedWarsItem pickaxe = context.getPickaxeItem(),
                axe = context.getAxeItem();

        return item.isToolPickaxe() && item.isInferior(pickaxe) || item.isToolAxe() && item.isInferior(axe);
    }

    public boolean hasArmor(BedWarsItem item) {
        if (!item.isType(BedShop.ARMOR)) return false;

        BedWarsItem armor = context.getArmorItem();

        return armor != null && armor.isType(BedShop.ARMOR) && armor.ordinal() >= item.ordinal();
    }
    
    // Métodos de moedas
    public int getCoins() {
        return context.getCoins();
    }
    
    public void addCoins(int amount) {
        context.addCoins(amount);
        
        // Adicionar também às moedas permanentes do BedMember
        if (member != null) {
            member.addCoins(amount);
        }
        
        getPlayer().sendMessage("§e+§6" + amount + " moedas §7(" + context.getCoins() + " total)");
    }
    
    public void removeCoins(int amount) {
        context.removeCoins(amount);
    }
    
    public boolean hasCoins(int amount) {
        return context.hasCoins(amount);
    }
}
