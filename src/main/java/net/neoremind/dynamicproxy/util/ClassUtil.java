package net.neoremind.dynamicproxy.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author zhangxu
 */
public class ClassUtil {
//
    //    /**
    //     * 取得指定类的package名。
    //     * <p>
    //     * 对于数组，此方法返回的是数组元素类型的package名。
    //     * </p>
    //     *
    //     * @param clazz 要查看的类
    //     *
    //     * @return package名，如果类为 <code>null</code> ，则返回<code>""</code>
    //     */
    //    public static String getPackageName(Class<?> clazz) {
    //        if (clazz == null) {
    //            return Emptys.EMPTY_STRING;
    //        }
    //
    //        return getPackageName(clazz.getName());
    //    }
    //
    //    /**
    //     * 取得指定类名的package名。
    //     * <p>
    //     * 对于数组，此方法返回的是数组元素类型的package名。
    //     * </p>
    //     *
    //     * @param javaClassName 要查看的类名
    //     *
    //     * @return package名，如果类名为空，则返回 <code>null</code>
    //     */
    //    public static String getPackageName(String javaClassName) {
    //        String friendlyClassName = toFriendlyClassName(javaClassName, false, null);
    //
    //        if (friendlyClassName == null) {
    //            return Emptys.EMPTY_STRING;
    //        }
    //
    //        int i = friendlyClassName.lastIndexOf('.');
    //
    //        if (i == -1) {
    //            return Emptys.EMPTY_STRING;
    //        }
    //
    //        return friendlyClassName.substring(0, i);
    //    }
    //
    //    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
    //        if (clazz == null) {
    //            return null;
    //        }
    //
    //        List<Class<?>> interfacesFound = Lists.newArrayList();
    //        getAllInterfaces(clazz, interfacesFound);
    //
    //        return interfacesFound;
    //    }
    //
    //    private static void getAllInterfaces(Class<?> clazz, List<Class<?>> interfacesFound) {
    //        while (clazz != null) {
    //            Class<?>[] interfaces = clazz.getInterfaces();
    //
    //            for (int i = 0; i < interfaces.length; i++) {
    //                if (!interfacesFound.contains(interfaces[i])) {
    //                    interfacesFound.add(interfaces[i]);
    //                    getAllInterfaces(interfaces[i], interfacesFound);
    //                }
    //            }
    //
    //            clazz = clazz.getSuperclass();
    //        }
    //    }
    //
    //    /**
    //     * 将Java类名转换成友好类名。
    //     *
    //     * @param javaClassName     Java类名
    //     * @param processInnerClass 是否将内联类分隔符 <code>'$'</code> 转换成 <code>'.'</code>
    //     *
    //     * @return 友好的类名。如果参数非法或空，则返回<code>null</code>。
    //     */
    //    static String toFriendlyClassName(String javaClassName, boolean processInnerClass, String defaultIfInvalid) {
    //        String name = StringUtils.trimToNull(javaClassName);
    //
    //        if (name == null) {
    //            return defaultIfInvalid;
    //        }
    //
    //        if (processInnerClass) {
    //            name = name.replace('$', '.');
    //        }
    //
    //        int length = name.length();
    //        int dimension = 0;
    //
    //        // 取得数组的维数，如果不是数组，维数为0
    //        for (int i = 0; i < length; i++, dimension++) {
    //            if (name.charAt(i) != '[') {
    //                break;
    //            }
    //        }
    //
    //        // 如果不是数组，则直接返回
    //        if (dimension == 0) {
    //            return name;
    //        }
    //
    //        // 确保类名合法
    //        if (length <= dimension) {
    //            return defaultIfInvalid; // 非法类名
    //        }
    //
    //        // 处理数组
    //        StringBuilder componentTypeName = new StringBuilder();
    //
    //        switch (name.charAt(dimension)) {
    //            case 'Z':
    //                componentTypeName.append("boolean");
    //                break;
    //
    //            case 'B':
    //                componentTypeName.append("byte");
    //                break;
    //
    //            case 'C':
    //                componentTypeName.append("char");
    //                break;
    //
    //            case 'D':
    //                componentTypeName.append("double");
    //                break;
    //
    //            case 'F':
    //                componentTypeName.append("float");
    //                break;
    //
    //            case 'I':
    //                componentTypeName.append("int");
    //                break;
    //
    //            case 'J':
    //                componentTypeName.append("long");
    //                break;
    //
    //            case 'S':
    //                componentTypeName.append("short");
    //                break;
    //
    //            case 'L':
    //                if (name.charAt(length - 1) != ';' || length <= dimension + 2) {
    //                    return defaultIfInvalid; // 非法类名
    //                }
    //
    //                componentTypeName.append(name.substring(dimension + 1, length - 1));
    //                break;
    //
    //            default:
    //                return defaultIfInvalid; // 非法类名
    //        }
    //
    //        for (int i = 0; i < dimension; i++) {
    //            componentTypeName.append("[]");
    //        }
    //
    //        return componentTypeName.toString();
    //    }

}
