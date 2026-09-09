package com.minecraft.core.bukkit.api.permission.regex;

import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.api.permission.injector.FieldReplacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PEXPermissionSubscriptionMap extends HashMap<String, Map<Permissible, Boolean>> {

    private static final long serialVersionUID = -3815816386187051557L;
    private static final AtomicReference<PEXPermissionSubscriptionMap> INSTANCE;
    private static FieldReplacer<PluginManager, Map> INJECTOR;

    static {
        INSTANCE = new AtomicReference<>();
    }

    private final PluginManager manager;

    private PEXPermissionSubscriptionMap(final Plugin plugin, final PluginManager manager, final Map<String, Map<Permissible, Boolean>> backing) {
        super(backing);
        this.manager = manager;
    }

    public static PEXPermissionSubscriptionMap inject(final Plugin plugin, final PluginManager manager) {
        final PEXPermissionSubscriptionMap map = PEXPermissionSubscriptionMap.INSTANCE.get();
        if (map != null) {
            return map;
        }
        if (PEXPermissionSubscriptionMap.INJECTOR == null) {
            PEXPermissionSubscriptionMap.INJECTOR = new FieldReplacer<>(manager.getClass(), "permSubs", Map.class);
        }
        final Map<String, Map<Permissible, Boolean>> backing = PEXPermissionSubscriptionMap.INJECTOR.get(manager);
        if (backing instanceof PEXPermissionSubscriptionMap) {
            return (PEXPermissionSubscriptionMap) backing;
        }
        final PEXPermissionSubscriptionMap wrappedMap = new PEXPermissionSubscriptionMap(plugin, manager, backing);
        if (PEXPermissionSubscriptionMap.INSTANCE.compareAndSet(null, wrappedMap)) {
            PEXPermissionSubscriptionMap.INJECTOR.set(manager, wrappedMap);
            return wrappedMap;
        }
        return PEXPermissionSubscriptionMap.INSTANCE.get();
    }

    public void uninject() {
        if (PEXPermissionSubscriptionMap.INSTANCE.compareAndSet(this, null)) {
            final Map<String, Map<Permissible, Boolean>> unwrappedMap = new HashMap<>(this.size());
            for (final Entry<String, Map<Permissible, Boolean>> entry : this.entrySet()) {
                if (entry.getValue() instanceof PEXSubscriptionValueMap) {
                    unwrappedMap.put(entry.getKey(), ((PEXSubscriptionValueMap) entry.getValue()).backing);
                }
            }
            PEXPermissionSubscriptionMap.INJECTOR.set(this.manager, unwrappedMap);
        }
    }

    @Override
    public Map<Permissible, Boolean> get(final Object key) {
        if (key == null) {
            return null;
        }
        Map<Permissible, Boolean> result = super.get(key);
        if (result == null) {
            result = new PEXSubscriptionValueMap((String) key, new WeakHashMap<>());
            super.put((String) key, result);
        } else if (!(result instanceof PEXSubscriptionValueMap)) {
            result = new PEXSubscriptionValueMap((String) key, result);
            super.put((String) key, result);
        }
        return result;
    }

    @Override
    public Map<Permissible, Boolean> put(final String key, Map<Permissible, Boolean> value) {
        if (!(value instanceof PEXSubscriptionValueMap)) {
            value = new PEXSubscriptionValueMap(key, value);
        }
        return super.put(key, value);
    }

    public static class PEXSubscriptionValueMap implements Map<Permissible, Boolean> {
        private final String permission;
        private final Map<Permissible, Boolean> backing;

        public PEXSubscriptionValueMap(final String permission, final Map<Permissible, Boolean> backing) {
            this.permission = permission;
            this.backing = backing;
        }

        public int size() {
            return this.backing.size();
        }

        public boolean isEmpty() {
            return this.backing.isEmpty();
        }

        public boolean containsKey(final Object key) {
            return this.backing.containsKey(key) || (key instanceof Permissible && ((Permissible) key).isPermissionSet(this.permission));
        }

        public boolean containsValue(final Object value) {
            return this.backing.containsValue(value);
        }

        public Boolean put(final Permissible key, final Boolean value) {
            return this.backing.put(key, value);
        }

        public Boolean remove(final Object key) {
            return this.backing.remove(key);
        }

        public void putAll(final Map<? extends Permissible, ? extends Boolean> m) {
            this.backing.putAll(m);
        }

        public void clear() {
            this.backing.clear();
        }

        public Boolean get(final Object key) {
            if (key instanceof Permissible) {
                final Permissible p = (Permissible) key;
                if (p.isPermissionSet(this.permission)) {
                    return p.hasPermission(this.permission);
                }
            }
            return this.backing.get(key);
        }

        public Set<Permissible> keySet() {
            int size = Bukkit.getOnlinePlayers().size();
            final Set<Permissible> pexMatches = new HashSet<Permissible>(size);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(this.permission)) {
                    pexMatches.add(player);
                }
            }
            return (Set<Permissible>) Sets.union((Set) pexMatches, (Set) this.backing.keySet());
        }

        public Collection<Boolean> values() {
            return this.backing.values();
        }

        public Set<Entry<Permissible, Boolean>> entrySet() {
            return this.backing.entrySet();
        }
    }
}
