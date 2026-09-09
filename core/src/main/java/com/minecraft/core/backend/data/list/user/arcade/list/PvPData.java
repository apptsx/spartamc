package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.pvp.PvPMember;

public class PvPData extends MemberData<PvPMember> {

    public PvPData(MySQLDatabase mysql, RedisDatabase redis) {
        super(mysql, redis, "members_pvp");
    }
}
