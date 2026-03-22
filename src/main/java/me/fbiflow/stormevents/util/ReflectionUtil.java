package me.fbiflow.stormevents.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    public static Object getFieldValue(String fieldName, Object object) {
        Object value;
        try {
            Field field = object.getClass().getField(fieldName);
            field.setAccessible(true);
            value = field.get(object);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
        return value;
    }
}
