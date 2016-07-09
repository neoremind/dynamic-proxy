package net.neoremind.dynamicproxy;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.wensiqun.asmsupport.utils.lang.ArrayUtils;
import net.neoremind.dynamicproxy.util.Emptys;
import net.neoremind.dynamicproxy.util.KeyAndValue;
import net.neoremind.dynamicproxy.util.ReflectionUtil;

/**
 * 方法签名
 *
 * @author zhangxu
 */
public class MethodSignature implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2236251542533774653L;
    private static final Map<Class<?>, Character> primitiveAbbreviations;
    private static final Map<Character, Class<?>> reverseAbbreviations;

    static {
        Map<Class<?>, Character> map = Maps.newHashMap();

        map.put(Boolean.TYPE, Character.valueOf('Z'));
        map.put(Byte.TYPE, Character.valueOf('B'));
        map.put(Short.TYPE, Character.valueOf('S'));
        map.put(Integer.TYPE, Character.valueOf('I'));
        map.put(Character.TYPE, Character.valueOf('C'));
        map.put(Long.TYPE, Character.valueOf('J'));
        map.put(Float.TYPE, Character.valueOf('F'));
        map.put(Double.TYPE, Character.valueOf('D'));
        map.put(Void.TYPE, Character.valueOf('V'));

        Map<Character, Class<?>> reverseMap = Maps.newHashMapWithExpectedSize(map.size());
        for (Map.Entry<Class<?>, Character> entry : map.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }

        primitiveAbbreviations = Collections.unmodifiableMap(map);
        reverseAbbreviations = Collections.unmodifiableMap(reverseMap);
    }

    private static void appendTo(StringBuilder buf, Class<?> type) {
        if (type.isPrimitive()) {
            buf.append(primitiveAbbreviations.get(type));
        } else if (type.isArray()) {
            buf.append('[');
            appendTo(buf, type.getComponentType());
        } else {
            buf.append('L').append(type.getName().replace('.', '/')).append(';');
        }
    }

    private static class SignaturePosition extends ParsePosition {
        SignaturePosition() {
            super(0);
        }

        SignaturePosition next() {
            return plus(1);
        }

        SignaturePosition plus(int addend) {
            setIndex(getIndex() + addend);
            return this;
        }
    }

    private static KeyAndValue<String, Class<?>[]> parse(String internal) {
        SignaturePosition pos = new SignaturePosition();
        int lparen = internal.indexOf('(', pos.getIndex());

        String name = internal.substring(0, lparen).trim();

        pos.setIndex(lparen + 1);

        List<Class<?>> params = Lists.newArrayList();
        while (pos.getIndex() < internal.length()) {
            char c = internal.charAt(pos.getIndex());
            if (Character.isWhitespace(c)) {
                pos.next();
                continue;
            }
            Character k = Character.valueOf(c);
            if (reverseAbbreviations.containsKey(k)) {
                params.add(reverseAbbreviations.get(k));
                pos.next();
                continue;
            }
            if (')' == c) {
                pos.next();
                break;
            }
            try {
                params.add(parseType(internal, pos));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("Method signature \"%s\" references unknown type",
                        internal), e);
            }
        }

        return new KeyAndValue<String, Class<?>[]>(name, params.toArray(Emptys.EMPTY_CLASS_ARRAY));
    }

    private static Class<?> parseType(String internal, SignaturePosition pos) throws ClassNotFoundException {
        int here = pos.getIndex();
        char c = internal.charAt(here);

        switch (c) {
            case '[':
                pos.next();
                Class<?> componentType = parseType(internal, pos);
                return Array.newInstance(componentType, 0).getClass();
            case 'L':
                pos.next();
                int type = pos.getIndex();
                int semi = internal.indexOf(';', type);

                String className = internal.substring(type, semi).replace('/', '.');

                pos.setIndex(semi + 1);
                return Class.forName(className);
            default:
                throw new IllegalArgumentException(String.format(
                        "Unexpected character at index %d of method signature \"%s\"", Integer.valueOf(here),
                        internal));
        }
    }

    private final String internal;

    public MethodSignature(Method method) {
        final StringBuilder buf = new StringBuilder(method.getName()).append('(');
        for (Class<?> p : method.getParameterTypes()) {
            appendTo(buf, p);
        }

        buf.append(')');
        this.internal = buf.toString();
    }

    public static String getName(String className, String methodName, Class<?>[] parameterTypes) {
        final StringBuilder buf = new StringBuilder(className).append(".").append(methodName).append('(');
        if (ArrayUtils.isNotEmpty(parameterTypes)) {
            for (Class<?> p : parameterTypes) {
                buf.append(p.getName()).append(",");
            }
        }
        buf.append(')');
        return buf.toString();
    }

    public Method toMethod(Class<?> type) {
        final KeyAndValue<String, Class<?>[]> info = parse(internal);

        return ReflectionUtil.getMethod(type, info.getKey(), info.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        MethodSignature other = (MethodSignature) o;
        return other.internal.equals(internal);
    }

    @Override
    public int hashCode() {
        return internal.hashCode();
    }

    @Override
    public String toString() {
        return internal;
    }
}
