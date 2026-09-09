package com.minecraft.core.bukkit.api.permission.regex;

import com.minecraft.core.bukkit.api.permission.injector.FieldReplacer;
import com.minecraft.core.bukkit.api.permission.injector.PermissionCheckResult;
import com.minecraft.core.bukkit.api.permission.injector.RegExpMatcher;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("rawtypes")
public class PermissiblePEX extends PermissibleBase {

    private static final FieldReplacer<PermissibleBase, Map> PERMISSIONS_FIELD;
    private static final FieldReplacer<PermissibleBase, List> ATTACHMENTS_FIELD;
    private static final Method CALC_CHILD_PERMS_METH;
    private static final AtomicBoolean LAST_CALL_ERRORED;
    private static final RegExpMatcher matcher = new RegExpMatcher();

    static {
        PERMISSIONS_FIELD = new FieldReplacer<>(PermissibleBase.class, "permissions", Map.class);
        ATTACHMENTS_FIELD = new FieldReplacer<>(PermissibleBase.class, "attachments", List.class);
        try {
            CALC_CHILD_PERMS_METH = PermissibleBase.class.getDeclaredMethod("calculateChildPermissions", Map.class, Boolean.TYPE, PermissionAttachment.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
        PermissiblePEX.CALC_CHILD_PERMS_METH.setAccessible(true);
        LAST_CALL_ERRORED = new AtomicBoolean(false);
    }

    protected final Player player;
    protected final Plugin plugin;
    protected final Map<String, PermissionCheckResult> cache;
    private final Map<String, PermissionAttachmentInfo> permissions;
    private final List attachments;
    private final Object permissionsLock;
    private Permissible previousPermissible;

    public PermissiblePEX(final Player player, final Plugin plugin) {
        super(player);
        this.previousPermissible = null;
        this.cache = new ConcurrentHashMap<>();
        this.permissionsLock = new Object();
        this.player = player;
        this.plugin = plugin;
        this.permissions = new LinkedHashMap<String, PermissionAttachmentInfo>() {
            private static final long serialVersionUID = 1L;

            public PermissionAttachmentInfo put(final String k, final PermissionAttachmentInfo v) {
                final PermissionAttachmentInfo existing = this.get(k);
                if (existing != null) {
                    return existing;
                }
                return super.put(k, v);
            }
        };
        PermissiblePEX.PERMISSIONS_FIELD.set(this, this.permissions);
        this.attachments = PermissiblePEX.ATTACHMENTS_FIELD.get(this);
        this.recalculatePermissions();
    }

    public Permissible getPreviousPermissible() {
        return this.previousPermissible;
    }

    public void setPreviousPermissible(final Permissible previousPermissible) {
        this.previousPermissible = previousPermissible;
    }

    public boolean hasPermission(final String permission) {
        final PermissionCheckResult res = this.permissionValue(permission);
        switch (res) {
            case UNDEFINED:
            case TRUE: {
                return res.toBoolean();
            }
            default: {
                if (super.isPermissionSet(permission)) {
                    return super.hasPermission(permission);
                }
                final Permission perm = this.player.getServer().getPluginManager().getPermission(permission);
                return (perm == null) ? Permission.DEFAULT_PERMISSION.getValue(this.player.isOp()) : perm.getDefault().getValue(this.player.isOp());
            }
        }
    }

    public boolean hasPermission(final Permission permission) {
        final PermissionCheckResult res = this.permissionValue(permission.getName());
        switch (res) {
            case UNDEFINED:
            case TRUE: {
                return res.toBoolean();
            }
            default: {
                if (super.isPermissionSet(permission.getName())) {
                    return super.hasPermission(permission);
                }
                return permission.getDefault().getValue(this.player.isOp());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void recalculatePermissions() {
        if (this.cache != null && this.permissions != null && this.attachments != null) {
            synchronized (this.permissionsLock) {
                this.clearPermissions();
                this.cache.clear();
                final ListIterator<PermissionAttachment> it = this.attachments.listIterator(this.attachments.size());
                while (it.hasPrevious()) {
                    final PermissionAttachment attach = it.previous();
                    this.calculateChildPerms(attach.getPermissions(), attach);
                }
                for (final Permission p : this.player.getServer().getPluginManager().getDefaultPermissions(this.isOp())) {
                    this.permissions.put(p.getName(), new PermissionAttachmentInfo(this.player, p.getName(), null, true));
                    this.calculateChildPerms(p.getChildren(), null);
                }
            }
        }
    }

    protected void calculateChildPerms(final Map<String, Boolean> children, final PermissionAttachment attachment) {
        try {
            PermissiblePEX.CALC_CHILD_PERMS_METH.invoke(this, children, false, attachment);
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPermissionSet(final String permission) {
        return super.isPermissionSet(permission) || this.permissionValue(permission) != PermissionCheckResult.UNDEFINED;
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        synchronized (this.permissionsLock) {
            return new LinkedHashSet<>(this.permissions.values());
        }
    }

    private PermissionCheckResult checkSingle(final String expression, final String permission, final boolean value) {
        if (matcher.isMatches(expression, permission)) {
            return PermissionCheckResult.fromBoolean(value);
        }
        return PermissionCheckResult.UNDEFINED;
    }

    protected PermissionCheckResult permissionValue(String permission) {
        try {
            Validate.notNull(permission, "Permissions being checked must not be null!");
            permission = permission.toLowerCase();
            PermissionCheckResult res = this.cache.get(permission);
            if (res != null) {
                return res;
            }
            res = PermissionCheckResult.UNDEFINED;
            synchronized (this.permissionsLock) {
                for (final PermissionAttachmentInfo pai : this.permissions.values()) {
                    if ((res = this.checkSingle(pai.getPermission(), permission, pai.getValue())) != PermissionCheckResult.UNDEFINED) {
                        break;
                    }
                }
            }
            if (res == PermissionCheckResult.UNDEFINED) {
                for (final Map.Entry<String, Boolean> ent : RegexPermissions.instance.getPermissionList().getParents(permission)) {
                    if ((res = this.permissionValue(ent.getKey())) != PermissionCheckResult.UNDEFINED) {
                        res = PermissionCheckResult.fromBoolean(res.toBoolean() == ent.getValue());
                        break;
                    }
                }
            }
            this.cache.put(permission, res);
            PermissiblePEX.LAST_CALL_ERRORED.set(false);
            return res;
        } catch (Throwable t) {
            if (PermissiblePEX.LAST_CALL_ERRORED.compareAndSet(false, true)) {
                t.printStackTrace();
            }
            return PermissionCheckResult.UNDEFINED;
        }
    }
}
