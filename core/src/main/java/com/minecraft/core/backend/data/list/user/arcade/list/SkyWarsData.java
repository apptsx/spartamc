package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.skywars.SkyMember;

public class SkyWarsData extends MemberData<SkyMember> {

    public SkyWarsData(MySQLDatabase mysql, RedisDatabase redis) {
        super(mysql, redis, "members_skywars");
    }
}
