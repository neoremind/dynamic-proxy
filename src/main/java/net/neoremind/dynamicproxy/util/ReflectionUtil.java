package net.neoremind.dynamicproxy.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author zhangxu
 */
public class ReflectionUtil {

    /**
     * 判断是否有超类
     *
     * @param clazz 目标类
     *
     * @return 如果有返回<code>true</code>，否则返回<code>false</code>
     */
    public static boolean hasSuperClass(Class<?> clazz) {
        return (clazz != null) && !clazz.equals(Object.class);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || StringUtils.isBlank(methodName)) {
            return null;
        }

        for (Class<?> itr = clazz; hasSuperClass(itr); ) {
            Method[] methods = itr.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                    return method;
                }
            }
            itr = itr.getSuperclass();
        }

        return null;

    }

    public static Method getMethod(Object object, String methodName, Class<?>... parameterTypes) {
        if (object == null || StringUtils.isBlank(methodName)) {
            return null;
        }

        for (Class<?> itr = object.getClass(); hasSuperClass(itr); ) {
            Method[] methods = itr.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                    return method;
                }
            }
            itr = itr.getSuperclass();
        }
        return null;
    }

    public static <T> T newInstance(final Class<T> clazz) {
        Constructor<?>[] constructors = getAllConstructorsOfClass(clazz, true);
        // impossible ?
        if (ArrayUtils.isEmpty(constructors)) {
            return null;
        }

        Optional<? extends Constructor<?>> optionalConstructor = getDefaultConstructor(constructors);
        if (!optionalConstructor.isPresent()) {
            throw new RuntimeException("No default non parameter constructor found for class " + clazz.getName());
        }

        try {
            T instance = (T) optionalConstructor.get().newInstance();
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Constructor<?>[] getAllConstructorsOfClass(final Class<?> clazz, boolean accessible) {
        if (clazz == null) {
            return null;
        }

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (ArrayUtils.isNotEmpty(constructors)) {
            AccessibleObject.setAccessible(constructors, accessible);
        }
        return constructors;
    }

    private static Optional<? extends Constructor<?>> getDefaultConstructor(Constructor<?>[] constructors) {
        if (ArrayUtils.isEmpty(constructors)) {
            return Optional.absent();
        }

        for (Constructor<?> constructor : constructors) {
            if (ArrayUtils.isEmpty(constructor.getParameterTypes())) {
                return Optional.of(constructor);
            }
        }
        return Optional.absent();
    }



}
