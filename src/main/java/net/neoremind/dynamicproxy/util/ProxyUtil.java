package net.neoremind.dynamicproxy.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import net.neoremind.dynamicproxy.DefaultProxyCreator;
import net.neoremind.dynamicproxy.ProxyCreator;

/**
 * 动态代理相关的工具类
 *
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午10:40:28
 */
public abstract class ProxyUtil {

    public static final Object[] EMPTY_ARGUMENTS = Emptys.EMPTY_OBJECT_ARRAY;
    public static final Class<?>[] EMPTY_ARGUMENT_TYPES = Emptys.EMPTY_CLASS_ARRAY;
    private static final Map<Class<?>, Class<?>> WRAPPER_CLASS_MAP;
    private static final Map<Class<?>, Object> NULL_VALUE_MAP;

    static {
        Map<Class<?>, Class<?>> wrappers = Maps.newHashMapWithExpectedSize(8);
        // @see ClassUtil
        wrappers.put(Integer.TYPE, Integer.class);
        wrappers.put(Character.TYPE, Character.class);
        wrappers.put(Boolean.TYPE, Boolean.class);
        wrappers.put(Short.TYPE, Short.class);
        wrappers.put(Long.TYPE, Long.class);
        wrappers.put(Float.TYPE, Float.class);
        wrappers.put(Double.TYPE, Double.class);
        wrappers.put(Byte.TYPE, Byte.class);
        WRAPPER_CLASS_MAP = Collections.unmodifiableMap(wrappers);

        // @see ClassUtil
        Map<Class<?>, Object> nullValues = Maps.newHashMapWithExpectedSize(8);
        nullValues.put(Integer.TYPE, Integer.valueOf(0));
        nullValues.put(Long.TYPE, Long.valueOf(0));
        nullValues.put(Short.TYPE, Short.valueOf((short) 0));
        nullValues.put(Byte.TYPE, Byte.valueOf((byte) 0));
        nullValues.put(Float.TYPE, Float.valueOf(0.0f));
        nullValues.put(Double.TYPE, Double.valueOf(0.0));
        nullValues.put(Character.TYPE, Character.valueOf((char) 0));
        nullValues.put(Boolean.TYPE, Boolean.FALSE);
        NULL_VALUE_MAP = Collections.unmodifiableMap(nullValues);
    }

    public static Class<?>[] getAllInterfaces(Class<?> cls) {
        return cls == null ? null : ClassUtil.getAllInterfaces(cls).toArray(Emptys.EMPTY_CLASS_ARRAY);
    }

    public static String getJavaClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }

        return clazz.getName();
    }

    public static Class<?> getWrapperClass(Class<?> primitiveType) {
        return WRAPPER_CLASS_MAP.get(primitiveType);
    }

    public static <T> T nullValue(Class<T> type) {
        @SuppressWarnings("unchecked")
        T result = (T) NULL_VALUE_MAP.get(type);

        return result;
    }

    public static ProxyCreator getInstance() {
        return DefaultProxyCreator.INSTANCE;
    }

    public static boolean isHashCode(Method method) {
        return "hashCode".equals(method.getName()) && Integer.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 0;
    }

    public static boolean isEqualsMethod(Method method) {
        return "equals".equals(method.getName()) && Boolean.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 1 && Object.class.equals(method.getParameterTypes()[0]);
    }

    public static String getProxyName(Class<?> targetClass, String prefix) {
        String className = ClassUtil.getPackageName(targetClass) + "." + prefix + targetClass.getSimpleName();
        return className;
    }

    public static String getProxyName(String targetClass, String prefix) {
        String className =
                ClassUtil.getPackageName(targetClass) + "." + prefix
                        + substringAfterLast(targetClass, ".");
        return className;
    }

    public static String substringAfterLast(String str, String separator) {
        if ((str == null) || (str.length() == 0)) {
            return str;
        }

        if ((separator == null) || (separator.length() == 0)) {
            return "";
        }

        int pos = str.lastIndexOf(separator);

        if ((pos == -1) || (pos == (str.length() - separator.length()))) {
            return "";
        }

        return str.substring(pos + separator.length());
    }

}
