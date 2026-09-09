package com.minecraft.core.backend.data.template;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface UserTemplate<T> {

    T save(T object);

    void delete(T object);

    void update(T object, String field);

    void startExpiration(T object);

    void cancelExpiration(T object);

    default T of(UUID id) {
        return of(id, false);
    }

    T of(UUID id, boolean storeInRedis);

    T of(String name);

    List<T> list();

    default List<T> filteredList(Predicate<T> filter) {
        return list().stream().filter(filter).collect(Collectors.toList());
    }

    Collection<T> ranking(String field, int limit);
}
