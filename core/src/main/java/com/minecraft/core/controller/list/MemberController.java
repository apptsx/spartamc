package com.minecraft.core.controller.list;

import com.minecraft.core.controller.Controller;
import com.minecraft.core.member.Member;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MemberController extends Controller<String, Member> {

    @Override
    public void save(Member member) {
        String arcade = member.getClass().getSimpleName().split("Member")[0].toLowerCase();

        getCache().put(arcade + ":" + member.getId(), member);
    }

    public <T extends Member> T of(UUID id, Class<T> generic) {
        String arcade = generic.getSimpleName().split("Member")[0].toLowerCase();

        Member member = of(arcade + ":" + id);

        return member != null ? generic.cast(member) : null;
    }

    public <T extends Member> T of(String name, Class<T> generic) {
        return list(generic)
                .stream().filter(member -> member.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public <T extends Member> void remove(UUID id, Class<T> memberClass) {
        String arcade = memberClass.getSimpleName().split("Member")[0].toLowerCase();

        getCache().remove(arcade + ":" + id);
    }

    public <T extends Member> List<T> list(Class<T> generic) {
        return list()
                .stream().filter(obj -> obj != null && generic.isAssignableFrom(obj.getClass()))
                .map(generic::cast)
                .collect(Collectors.toList());
    }

    public <T extends Member> List<T> list(Class<T> generic, Predicate<T> filter) {
        return list(generic).stream().filter(filter).collect(Collectors.toList());
    }
}
