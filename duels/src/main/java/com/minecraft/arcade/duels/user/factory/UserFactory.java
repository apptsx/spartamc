package com.minecraft.arcade.duels.user.factory;

import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.util.list.loader.ClassLoader;
import com.minecraft.arcade.duels.Duels;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UserFactory {

    private static final List<Class<? extends User>> userList =
            ClassLoader.getClassesForPackage(Duels.getInstance(), Constant.SOURCE_DIR + ".arcade.duels.user.factory.list")
                    .stream()
                    .filter(user -> user != null && User.class.isAssignableFrom(user))
                    .map(user -> (Class<? extends User>) user) // Faz o cast para Class<? extends User>
                    .collect(Collectors.toList());

    public static Class<? extends User> getUser(Arcade arcade) {
        String arcadeName = arcade.getName().toLowerCase();
        
        Class<? extends User> found = userList.stream()
                .filter(user -> user.getSimpleName().toLowerCase().startsWith(arcadeName))
                .findFirst()
                .orElse(null);
        
        if (found == null) {
            Core.getLogger().warning("[UserFactory] Nenhum User encontrado para " + arcade.getName() + ", usando User padrão");
        }
        
        return found;
    }

    public static void create(Arcade arcade, DuelMember member, Arena arena, Join join) {
        Class<? extends User> userClass = getUser(arcade);

        if (userClass == null) {
            instantiateDefaultUser(member, arena, join);
            return;
        }

        try {
            Constructor<? extends User> constructor = userClass.getConstructor(DuelMember.class, Arena.class, Join.class);

            constructor.newInstance(member, arena, join);
        } catch (NoSuchMethodException e) {
            Core.getLogger().log(Level.SEVERE, "Construtor não encontrado para a classe de usuário: " + userClass.getName(), e);
            instantiateDefaultUser(member, arena, join);
        } catch (Exception e) {
            Core.getLogger().log(Level.SEVERE, "Falha ao inicializar o usuário de " + member.getId() + " para a classe " + userClass.getName(), e);
            instantiateDefaultUser(member, arena, join);
        }
    }

    private static void instantiateDefaultUser(DuelMember member, Arena arena, Join join) {
        new User(member, arena, join);
    }
}
