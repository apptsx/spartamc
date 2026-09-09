package com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.type;

import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.bedwars.user.User;

public class FinalKillTop extends Top {

    public FinalKillTop() {
        super(TopType.FINAL_KILL);
    }

    @Override
    public int getStatistic(User user) {
        return user.getContext().getFinalKill();
    }
}
