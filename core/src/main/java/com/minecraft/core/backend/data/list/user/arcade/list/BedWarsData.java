package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.bedwars.BedMember;

public class BedWarsData extends MemberData<BedMember> {

    public BedWarsData(MySQLDatabase mysql, RedisDatabase redis) {
        super(mysql, redis, "members_bedwars");
    }
}
