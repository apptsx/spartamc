package com.minecraft.arcade.pvp.user;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.pvp.PvPMember;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User extends UserModel {

    private final PvPMember member;

    private final Arena arena;

    private boolean isProtected = true, launcher;

    public User(PvPMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;
    }

    @Override
    public void handle() {
        Account account = getAccount();

        Player player = account.player();

        if (arena != null) {
            setSidebar(new Sidebar(player, Core.getServerType().name()));

            arena.join(player);
        }
    }

    public Arcade getArcade() {
        return arena.getArcade();
    }
}
