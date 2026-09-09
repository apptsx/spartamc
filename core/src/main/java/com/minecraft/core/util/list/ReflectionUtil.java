package com.minecraft.core.util.list;

import com.minecraft.core.util.list.reflection.FieldAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtil {

    public static void setValue(String field, Class<?> clazz, Object instance, Object value) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Object getValue(String field, Class<?> clazz, Object instance) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void setValue(String field, Object instance, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Object getValue(String field, Object instance) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void setValue(Object instance, String fieldName, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void setAccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        if (field.getModifiers() != (field.getModifiers() & ~Modifier.FINAL)) {
            try {
                getFieldAccessor(Field.class, "modifiers").set(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (Exception e) {
                try {
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Seta o valor de um field através um {@link FieldAccessor}
     *
     * @param field  O field para modificar.
     * @param target O alvo que contém o field para modificar.
     * @param value  O novo valor do field.
     */
    public static void setFieldValue(Field field, Object target, Object value) {
        new FieldAccessor<>(field, true).set(target, value);
    }

    /**
     * Retorna o valor de um field através de um {@link FieldAccessor}
     *
     * @param field  O field para pegar o valor.
     * @param target O alvo que contém o field para pegar.
     * @return O valor do field.
     */
    public static Object getFieldValue(Field field, Object target) {
        return new FieldAccessor<>(field, true).get(target);
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        while ((clazz != null) && (clazz != Object.class)) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     */
    public static FieldAccessor<Object> getFieldAccessor(Class clazz, int index) {
        return getFieldAccessor(clazz, index, null);
    }

    /**
     */
    public static FieldAccessor<Object> getFieldAccessor(Class clazz, String fieldName) {
        return getFieldAccessor(clazz, fieldName, null);
    }

    /**
     */
    public static <T> FieldAccessor<T> getFieldAccessor(Class clazz, int index, Class<T> fieldType) {
        return getFieldAccessor(clazz, null, index, fieldType);
    }

    /**
     */
    public static <T> FieldAccessor<T> getFieldAccessor(Class clazz, String fieldName, Class<T> fieldType) {
        return getFieldAccessor(clazz, fieldName, 0, fieldType);
    }

    /**
     * Método utilizado para pegar um {@link Field} por índice.
     *
     * @param clazz     A classe para buscar os fields.
     * @param fieldName O nome do field, caso for null irá ignorar o nome.
     * @param index     O índice para pegar o field, caso chegue em 0 irá retornar o field.
     * @param fieldType O tipo do field, caso for null irá ignorar o tipo.
     * @return O {@link Field} representado por um {@link FieldAccessor}
     */
    public static <T> FieldAccessor<T> getFieldAccessor(Class clazz, String fieldName, int index, Class<T> fieldType) {
        int indexCopy = index;
        for (final Field field : clazz.getDeclaredFields()) {
            if ((fieldName == null || fieldName.equals(field.getName())) && (fieldType == null || fieldType.equals(field.getType())) && index-- == 0) {
                return new FieldAccessor<>(field, true);
            }
        }

        String message = " with index " + indexCopy;
        if (fieldName != null) {
            message += " and name " + fieldName;
        }
        if (fieldType != null) {
            message += " and list " + fieldType;
        }

        throw new IllegalArgumentException("Cannot find field " + message);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    public static <T> T getValue(Object instance, String fieldName) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}