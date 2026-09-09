package com.minecraft.lobby.user;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.Member;
import com.minecraft.lobby.Lobby;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class User extends UserModel {

    public User(UUID id) {
        super(id, Join.PLAYER);
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        if (player != null) {
            setSidebar(new Sidebar(player, Constant.SERVER_TITLE));

            Lobby.getLoader().getArchitect().join(player);
        }
    }

    public <T extends Member> T getMember(Class<T> generic) {
        return Core.getMemberController().of(getAccount().getId(), generic);
    }
}
