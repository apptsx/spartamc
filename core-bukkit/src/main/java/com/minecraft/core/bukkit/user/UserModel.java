package com.minecraft.core.bukkit.user;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.combat.Combat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class UserModel {

    @Getter
    private static final Set<UserModel> list = new HashSet<>();

    private Account account;
    private Sidebar sidebar;

    private Tag tag;
    private Combat combat = new Combat();

    private Join join;

    public UserModel(UUID id, Join join) {
        this.account = Core.getAccountController().of(id);

        if (account != null) {
            Tag tag = account.getTag();

            this.tag = tag == null ? Tag.MEMBER : tag;
        }

        this.join = join;

        list.add(this);
    }

    public abstract void handle();

    public static UserModel of(UUID id) {
        return list.stream().filter(user -> user.getAccount().getId().equals(id)).findFirst().orElse(null);
    }

    public static void remove(UUID id) {
        list.removeIf(user -> user.getAccount().getId().equals(id));
    }

    public static boolean has(UUID id) {
        return list.stream().anyMatch(user -> user.getAccount().getId().equals(id));
    }

    public static List<UserModel> list(Predicate<UserModel> filter) {
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public boolean inCombat() {
        return combat != null && combat.isValid();
    }

    public void setCombat(Player target) {
        if (target == null) return;

        if (combat.isValid() && combat.getTarget().getUniqueId().equals(target.getUniqueId())) return;

        combat.update(target);
    }

    public boolean inState(ArcadeState state) {
        return getState().equals(state);
    }

    public ArcadeState getState() {
        return account.getArcadeRoute() != null && account.getArcadeRoute().getState() != null
                ? account.getArcadeRoute().getState()
                : ArcadeState.ALIVE;
    }

    public void setState(ArcadeState state) {
        account.setArcadeState(state);
    }

    /* Join Methods */
    public boolean isJoin(Join join) {
        return this.join.equals(join);
    }

    public boolean isPlayer() {
        return isJoin(Join.PLAYER);
    }

    public boolean isVanish() {
        return isJoin(Join.VANISH);
    }

    public boolean isSpectator() {
        return isJoin(Join.SPECTATOR);
    }
}
