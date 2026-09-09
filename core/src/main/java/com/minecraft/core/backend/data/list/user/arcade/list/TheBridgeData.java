package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.thebridge.TheBridgeMember;

public class TheBridgeData extends MemberData<TheBridgeMember> {

    public TheBridgeData(MySQLDatabase mysql, RedisDatabase redis) {
        super(mysql, redis, "members_thebridge");
    }
}
