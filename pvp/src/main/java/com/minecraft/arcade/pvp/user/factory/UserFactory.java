package com.minecraft.arcade.pvp.user.factory;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.util.list.loader.ClassLoader;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UserFactory {

    private final static List<Class<?>> userList = ClassLoader.getClassesForPackage(PvP.getInstance(), Constant.SOURCE_DIR + ".arcade.pvp.user.factory.list")
            .stream()
            .filter(user -> user != null && User.class.isAssignableFrom(user))
            .collect(Collectors.toList());

    public static User create(ArcadeCategory arcade, PvPMember member, Arena arena, Join join) {
        Class<?> userClass = userList.stream()
                .filter(user -> user.getSimpleName().toLowerCase().startsWith(arcade.getName().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (userClass == null)
            return new User(member, arena, join);

        try {
            Constructor<?> constructor = userClass.getConstructor(PvPMember.class, Arena.class, Join.class);

            return (User) constructor.newInstance(member, arena, join);
        } catch (Exception e) {
            Core.getLogger().log(Level.SEVERE, "Não foi possível inicializar o usuário de " + member.getId(), e);

            return new User(member, arena, join);
        }
    }
}
