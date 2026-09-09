package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.hungergames.HungerGamesMember;

public class HungerGamesData extends MemberData<HungerGamesMember> {

    public HungerGamesData(MySQLDatabase mysql, RedisDatabase redis) {
        super(mysql, redis, "members_hungergames");
    }
}