package com.minecraft.core.controller;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public abstract class Controller<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public abstract void save(V value);

    public void remove(K key) {
        cache.remove(key);
    }

    public V of(K key) {
        return cache.get(key);
    }

    public V of(Predicate<V> predicate) {
        return filter(predicate).stream().findFirst().orElse(null);
    }

    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    public boolean isPresent(Predicate<V> filter) {
        return list().stream().anyMatch(filter);
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public int size() {
        return cache.size();
    }

    public List<V> filter(Predicate<V> predicate) {
        return cache.values().stream().filter(predicate).collect(Collectors.toList());
    }

    public List<V> list() {
        return new ArrayList<>(cache.values());
    }
}
