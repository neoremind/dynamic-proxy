package net.neoremind.dynamicproxy.util;

/**
 * @author zhangxu
 */
public class ObjectUtil {

    public static <T, S extends T> T defaultIfNull(T object, S defaultValue) {
        return object == null ? defaultValue : object;
    }

}
