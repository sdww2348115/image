package com.sdww8591.image.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionUtil {

    private ReflectionUtil() {}

    public static Field findFieldAndSetAccessible(Class<?> clazz, String fieldName) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(StringUtils.isNotEmpty(fieldName));

        Field field = ReflectionUtils.findField(clazz, fieldName);
        if (Objects.nonNull(field)) {
            field.setAccessible(true);
        }
        return field;
    }
}
