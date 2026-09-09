package com.minecraft.core.bukkit.api.permission.regex;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.minecraft.core.bukkit.api.permission.injector.FieldReplacer;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PermissionList extends HashMap<String, Permission> {

    private static final long serialVersionUID = 1L;
    private static final Map<Class<?>, FieldReplacer<Permission, Map>> CHILDREN_MAPS;
    private static FieldReplacer<PluginManager, Map> INJECTOR;

    static {
        CHILDREN_MAPS = new HashMap<>();
    }

    private final Multimap<String, Entry<String, Boolean>> childParentMapping;

    public PermissionList() {
        this.childParentMapping = (Multimap<String, Entry<String, Boolean>>) Multimaps.synchronizedMultimap((Multimap) HashMultimap.create());
    }

    public PermissionList(final Map<? extends String, ? extends Permission> existing) {
        super(existing);
        this.childParentMapping = (Multimap<String, Entry<String, Boolean>>) Multimaps.synchronizedMultimap((Multimap) HashMultimap.create());
    }

    public static PermissionList inject(final PluginManager manager) {
        if (PermissionList.INJECTOR == null) {
            PermissionList.INJECTOR = new FieldReplacer<>(manager.getClass(), "permissions", Map.class);
        }
        final Map existing = PermissionList.INJECTOR.get(manager);
        final PermissionList list = new PermissionList(existing);
        PermissionList.INJECTOR.set(manager, list);
        return list;
    }

    private FieldReplacer<Permission, Map> getFieldReplacer(final Permission perm) {
        FieldReplacer<Permission, Map> ret = PermissionList.CHILDREN_MAPS.get(perm.getClass());
        if (ret == null) {
            ret = new FieldReplacer<>(perm.getClass(), "children", Map.class);
            PermissionList.CHILDREN_MAPS.put(perm.getClass(), ret);
        }
        return ret;
    }

    private void removeAllChildren(final String perm) {
        this.childParentMapping.entries().removeIf(stringEntryEntry -> stringEntryEntry.getValue().getKey().equals(perm));
    }

    @Override
    public Permission put(final String k, final Permission v) {
        for (final Entry<String, Boolean> ent : v.getChildren().entrySet()) {
            this.childParentMapping.put(ent.getKey(), new SimpleEntry<>(v.getName(), ent.getValue()));
        }
        final FieldReplacer<Permission, Map> repl = this.getFieldReplacer(v);
        repl.set(v, new NotifyingChildrenMap(v));
        return super.put(k, v);
    }

    @Override
    public Permission remove(final Object k) {
        final Permission ret = super.remove(k);
        if (ret != null) {
            this.removeAllChildren(k.toString());
            this.getFieldReplacer(ret).set(ret, new LinkedHashMap(ret.getChildren()));
        }
        return ret;
    }

    @Override
    public void clear() {
        this.childParentMapping.clear();
        super.clear();
    }

    public Collection<Entry<String, Boolean>> getParents(final String permission) {
        return this.childParentMapping.get(permission.toLowerCase());
    }

    private class NotifyingChildrenMap extends LinkedHashMap<String, Boolean> {
        private static final long serialVersionUID = 1L;
        private final Permission perm;

        public NotifyingChildrenMap(final Permission perm) {
            this.perm = perm;
        }

        @Override
        public Boolean remove(final Object perm) {
            this.removeFromMapping(String.valueOf(perm));
            return super.remove(perm);
        }

        private void removeFromMapping(final String child) {
            PermissionList.this.childParentMapping.get(child).removeIf(stringBooleanEntry -> stringBooleanEntry.getKey().equals(this.perm.getName()));
        }

        @Override
        public Boolean put(final String perm, final Boolean val) {
            PermissionList.this.childParentMapping.put(perm, new SimpleEntry(this.perm.getName(), val));
            return super.put(perm, val);
        }

        @Override
        public void clear() {
            PermissionList.this.removeAllChildren(this.perm.getName());
            super.clear();
        }
    }
}
