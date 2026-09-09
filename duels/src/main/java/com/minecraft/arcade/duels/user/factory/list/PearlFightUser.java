package com.minecraft.arcade.duels.user.factory.list;

import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PearlFightUser extends User {

    private int lives = 3;

    public PearlFightUser(DuelMember member, Arena arena, Join join) {
        super(member, arena, join);
    }

    public void removeLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public boolean hasLives() {
        return lives > 0;
    }

    public boolean isDead() {
        return lives <= 0;
    }
}

