package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RedirectData {

    private final RedisDatabase redis;

    private final transient String REDIRECT_KEY = "redirect:";

    public void save(Redirect redirect) {
        redis.save(REDIRECT_KEY + redirect.getSender(), redirect, 5);
    }

    public Redirect of(UUID sender) {
        return redis.load(REDIRECT_KEY + sender, Redirect.class);
    }

    public void delete(UUID sender) {
        redis.delete(REDIRECT_KEY + sender);
    }

    public List<Redirect> list() {
        return redis.loadAll(REDIRECT_KEY, Redirect.class);
    }
}
