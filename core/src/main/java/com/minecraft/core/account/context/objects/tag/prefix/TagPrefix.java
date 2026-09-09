package com.minecraft.core.account.context.objects.tag.prefix;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum TagPrefix {

    DEFAULT("Padrão"),
    COLOR("Cor"),
    BRACKETS("Colchetes (Maiúsculo)"),
    BRACKETS_NORMAL_CASE("Colchetes (Normal)"),
    DEFAULT_GRAY("Padrão (Cinza)"),
    DEFAULT_WHITE("Padrão (Branco)");

    private final String name;

    public static TagPrefix of(String name) {
        return list(prefix -> prefix.name().equalsIgnoreCase(name) || prefix.getName().equalsIgnoreCase(name))
                .stream().findFirst().orElse(null);
    }

    public static List<TagPrefix> list() {
        return Stream.of(values()).collect(Collectors.toList());
    }

    public static List<TagPrefix> list(Predicate<TagPrefix> filter) {
        return list().stream().filter(filter).collect(Collectors.toList());
    }

    public int getColorId() {
        return 15 - ordinal();
    }
}
