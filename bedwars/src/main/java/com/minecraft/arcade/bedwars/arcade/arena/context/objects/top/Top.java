package com.minecraft.arcade.bedwars.arcade.arena.context.objects.top;

import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums.TopCategory;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.user.TopUser;
import com.minecraft.arcade.bedwars.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public abstract class Top {

    private final TopType type;

    private final List<TopUser> users = new ArrayList<>();

    public abstract int getStatistic(User user);

    public void setTop(UUID id, int value) {
        // Se já estiver no TOP, apenas atualizar
        if (inTop(id)) {
            TopUser top = getUser(id);

            top.setValue(value);

            int indexOf = users.indexOf(top);

            if (indexOf >= 0)
                users.set(indexOf, top);
        } else
            // Caso não esteja, incrementar
            users.add(new TopUser(id, value));
    }

    public List<TopUser> getOrderedUsers() {
        return users.stream().sorted(Comparator.comparingInt(TopUser::getValue).reversed()).collect(Collectors.toList());
    }

    public List<TopUser> getTopThird() {
        return getOrderedUsers().stream().limit(3).collect(Collectors.toList());
    }

    public TopUser getUser(UUID id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
    }

    public TopUser getUser(TopCategory category) {
        int index = category.ordinal(); // 0 = Primeiro lugar, 1 = Segundo lugar, 2 = Terceiro lugar, ...

        return getOrderedUsers().get(index);
    }

    public int getUserPosition(User user) {
        TopUser top = getUser(user.getAccount().getId());

        return top != null ? users.indexOf(top) : 99;
    }

    public boolean inTop(UUID id) {
        return getUser(id) != null;
    }
}
