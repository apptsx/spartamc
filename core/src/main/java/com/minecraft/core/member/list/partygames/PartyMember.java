package com.minecraft.core.member.list.partygames;

import com.minecraft.core.member.Member;

import lombok.Getter;
import java.util.UUID;

@Getter
public class PartyMember extends Member {


    public PartyMember(UUID id, String name) {
        super(id, name);

    }

    @Override
    public void save(String... fields) {

    }
}
