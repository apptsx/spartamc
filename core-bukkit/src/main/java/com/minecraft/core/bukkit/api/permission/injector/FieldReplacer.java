package com.minecraft.core.bukkit.api.permission.injector;

import java.lang.reflect.Field;

public class FieldReplacer<Instance, Type> {
    private final Class<Type> requiredType;
    private final Field field;

    public FieldReplacer(final Class<? extends Instance> clazz, final String fieldName, final Class<Type> requiredType) {
        this.requiredType = requiredType;
        this.field = getField(clazz, fieldName);
        if (this.field == null) {
            throw new ExceptionInInitializerError("No such field " + fieldName + " in class " + clazz);
        }
        this.field.setAccessible(true);
        if (!requiredType.isAssignableFrom(this.field.getType())) {
            throw new ExceptionInInitializerError("Field of wrong type");
        }
    }

    private static Field getField(Class<?> clazz, final String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    public Type get(final Instance instance) {
        try {
            return this.requiredType.cast(this.field.get(instance));
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public void set(final Instance instance, final Type newValue) {
        try {
            this.field.set(instance, newValue);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
