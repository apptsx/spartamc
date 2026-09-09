package com.minecraft.arcade.duels.user.factory.list;

import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoxingUser extends User {

    private int hits = 0;

    public BoxingUser(DuelMember member, Arena arena, Join join) {
        super(member, arena, join);
    }

    public boolean itWon() {
        return hits >= 100;
    }
}
