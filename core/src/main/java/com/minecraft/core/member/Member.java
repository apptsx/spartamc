package com.minecraft.core.member;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.backend.database.redis.message.types.account.AccountGlobalMessage;
import com.minecraft.core.member.context.MemberContext;
import com.minecraft.core.member.context.elo.Elo;
import com.minecraft.core.member.context.level.LevelController;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.context.stats.ArcadeStats;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public abstract class Member {

    private final UUID id;
    private String name;

    private MemberContext context;

    public Member(UUID id, String name) {
        this.id = id;
        this.name = name;

        this.context = new MemberContext();
    }

    public abstract void save(String... fields);

    public Account getAccount() {
        return Core.getAccountData().of(id);
    }

    public void setName(String name) {
        this.name = name;
        save("name");
    }

    public void saveContext(MemberContext context) {
        this.context = context;
        save("context");
    }

    public long getJoinedAt() {
        return context.getJoinedAt();
    }

    public List<MenuMetadata> getMenus() {
        return context.getMenus();
    }

    public String getBase64(ArcadeCategory arcade) {
        return getMenus().stream()
                .filter(menu -> menu.getArcade().equals(arcade))
                .findFirst()
                .map(MenuMetadata::getBase64)
                .orElse("...");
    }

    public MenuMetadata getMenu(ArcadeCategory arcade) {
        return getMenus().stream().filter(data -> data.getArcade().equals(arcade)).findFirst().orElse(null);
    }

    public void addMenu(MenuMetadata... menus) {
        getMenus().addAll(Arrays.asList(menus));
        saveContext(context);
    }

    public void removeMenu(ArcadeCategory... arcades) {
        Arrays.asList(arcades).forEach(arcade ->
                getMenus().removeIf(menu -> menu.getArcade().equals(arcade)));
        saveContext(context);
    }

    public void updateMenu(ArcadeCategory arcade, String base64) {
        MenuMetadata data = getMenu(arcade);

        if (data != null) {
            data.setBase64(base64);
            data.setLastUpdate(System.currentTimeMillis());

            int indexOf = getMenus().indexOf(data);

            if (indexOf >= 0) {
                getMenus().set(indexOf, data);
                saveContext(context);
            }
        }
    }

    public Map<ArcadeCategory, ArcadeStats> getStats() {
        return context.getStats();
    }

    public void addStats(ArcadeStats stats) {
        getStats().put(stats.getArcade(), stats);
        saveContext(context);
    }

    public void addMultipleStats(ArcadeStats... list) {
        Arrays.asList(list).forEach(stats -> getStats().put(stats.getArcade(), stats));
        saveContext(context);
    }

    public ArcadeStats getStats(ArcadeCategory arcade) {
        ArcadeStats stats = getStats().get(arcade);

        if (stats == null) {
            this.addMultipleStats(new ArcadeStats(arcade));

            stats = getStats().get(arcade);
        }

        return stats;
    }

    public void removeStats(ArcadeCategory arcade) {
        getStats().remove(arcade);
        saveContext(context);
    }

    public void updateStats(ArcadeStats stats) {
        getStats().put(stats.getArcade(), stats);
        saveContext(context);
    }

    public int getLevel() {
        return context.getLevel();
    }

    public int getMinLevelXp() {
        return 300;
    }

    public int getWeekLevelXp() {
        return context.getWeekLevelXp();
    }

    public int getMonthLevelXp() {
        return context.getMonthLevelXp();
    }

    public int getLevelXp() {
        return context.getLevelXp();
    }

    public int getCoins() {
        return context.getCoins();
    }

    public Elo getElo() {
        return context.getElo();
    }

    public Elo getNextElo() {
        return getElo() != Elo.IMMORTAL ? Elo.values()[getElo().ordinal() + 1] : null;
    }

    public int getEloXp() {
        return context.getEloXp();
    }

    public int getWeekEloXp() {
        return context.getWeekEloXp();
    }

    public int getMonthEloXp() {
        return context.getMonthEloXp();
    }

    public void setEloXp(int eloXp) {
        if (eloXp >= 0) {
            context.setEloXp(eloXp);
            saveContext(context);
        }
    }

    public void checkElo(int xp) {
        Elo elo = getElo();

        if (elo.isHighest()) return;

        Elo next = getNextElo();

        if (getEloXp() + xp >= next.getMinXp()) {
            new AccountGlobalMessage(id, "§eVocê acabou de subir de patente! A sua patente nova é " + next.getFullName() + "§e!").send();

            context.setElo(next);
            saveContext(context);
        }
    }

    public void addEloXp(int xp) {
        if (xp > 0) {
            context.setWeekEloXp(context.getWeekEloXp() + xp);
            context.setMonthEloXp(context.getMonthEloXp() + xp);

            setEloXp(getEloXp() + xp);
            checkElo(xp);
        }
    }

    public void removeEloXp(int xp) {
        if (xp > 0 && getEloXp() >= xp) {
            setEloXp(getEloXp() - xp);
        }
    }

    public void setLevel(int level) {
        if (level >= 0) {
            context.setLevel(level);
            saveContext(context);
        }
    }

    public void setLevelXp(int levelXp) {
        if (levelXp >= 0) {
            context.setLevelXp(levelXp);
            saveContext(context);
        }
    }

    public void addLevelXp(int levelXp) {
        if (levelXp > 0) {
            context.setWeekLevelXp(context.getWeekLevelXp() + levelXp);
            context.setMonthLevelXp(context.getMonthLevelXp() + levelXp);

            setLevelXp(getLevelXp() + levelXp);
            checkLevel(levelXp);
        }
    }

    public void removeLevelXp(int levelXp) {
        if (levelXp > 0 && getLevelXp() >= levelXp) {
            setLevelXp(getLevelXp() - levelXp);
        }
    }

    public void checkLevel(int xp) {
        if (getLevelXp() + xp >= getMinLevelXp()) {
            addLevel();
            setLevelXp(0);
        }
    }

    public void addLevel() {
        setLevel(getLevel() + 1);
    }

    public String getLevelId(int level) {
        Account account = getAccount();

        if (account != null && account.isUsingFake())
            level = 0;

        return LevelController.getDefaultLevelColor(level);
    }

    public String getLevelId() {
        return getLevelId(getLevel());
    }

    public String getNextLevelId() {
        return getLevelId(getLevel() + 1);
    }

    public void setCoins(int coins) {
        if (coins > 0) {
            context.setCoins(coins);
            saveContext(context);
        }
    }

    public void addCoins(int coins) {
        if (coins > 0)
            setCoins(getCoins() + coins);
    }

    public void removeCoins(int coins) {
        if (coins > 0 && getCoins() >= coins)
            setCoins(getCoins() - coins);
    }
}