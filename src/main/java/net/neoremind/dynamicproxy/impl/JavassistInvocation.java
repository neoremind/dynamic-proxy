package net.neoremind.dynamicproxy.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Maps;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import net.neoremind.dynamicproxy.Invocation;
import net.neoremind.dynamicproxy.util.JavassistUtil;
import net.neoremind.dynamicproxy.util.ObjectUtil;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午10:57:37
 */
public abstract class JavassistInvocation implements Invocation {

    private static Map<ClassLoader, Map<String, WeakReference<Class<?>>>> loaderToClassCache = Maps.newHashMap();

    private final Object proxy;

    private final Object target;

    private final Method method;

    private final Object[] arguments;

    private static String createCastExpression(Class<?> type, String objectToCast) {
        if (!type.isPrimitive()) {
            return "( " + ProxyUtil.getJavaClassName(type) + " )" + objectToCast;
        }

        return "( ( " + ProxyUtil.getWrapperClass(type).getName() + " )" + objectToCast + " )." + type.getName()
                + "Value()";
    }

    private static Class<?> createInvocationClass(ClassLoader classLoader, Method interfaceMethod)
            throws CannotCompileException {
        CtClass ctClass =
                JavassistUtil.createClass(
                        getSimpleName(interfaceMethod.getDeclaringClass()) + "_" + interfaceMethod.getName()
                                + "_invocation", JavassistInvocation.class);
        CtConstructor constructor =
                new CtConstructor(JavassistUtil.resolve(new Class[] {Object.class, Object.class, Method.class,
                        Object[].class}), ctClass);
        constructor.setBody("{\n\tsuper($$);\n}");
        ctClass.addConstructor(constructor);
        CtMethod proceedMethod =
                new CtMethod(JavassistUtil.resolve(Object.class), "proceed", JavassistUtil.resolve(new Class[0]),
                        ctClass);
        Class<?>[] argumentTypes = interfaceMethod.getParameterTypes();
        StringBuilder builder = new StringBuilder("{\n");
        if (!Void.TYPE.equals(interfaceMethod.getReturnType())) {
            builder.append("\treturn ");
            if (interfaceMethod.getReturnType().isPrimitive()) {
                builder.append("new ");
                builder.append(ProxyUtil.getWrapperClass(interfaceMethod.getReturnType()).getName());
                builder.append("( ");
            }
        } else {
            builder.append("\t");
        }

        builder.append("( (");
        builder.append(ProxyUtil.getJavaClassName(interfaceMethod.getDeclaringClass()));
        builder.append(" )getTarget() ).");
        builder.append(interfaceMethod.getName());
        builder.append("(");
        for (int i = 0; i < argumentTypes.length; ++i) {
            final Class<?> argumentType = argumentTypes[i];
            builder.append(createCastExpression(argumentType, "getArguments()[" + i + "]"));
            if (i != argumentTypes.length - 1) {
                builder.append(", ");
            }
        }

        if (!Void.TYPE.equals(interfaceMethod.getReturnType()) && interfaceMethod.getReturnType().isPrimitive()) {
            builder.append(") );\n");
        } else {
            builder.append(");\n");
        }

        if (Void.TYPE.equals(interfaceMethod.getReturnType())) {
            builder.append("\treturn null;\n");
        }

        builder.append("}");
        String body = builder.toString();
        proceedMethod.setBody(body);
        ctClass.addMethod(proceedMethod);

        @SuppressWarnings("deprecation")
        Class<?> invocationClass = ctClass.toClass(classLoader);
        return invocationClass;
    }

    private static Map<String, WeakReference<Class<?>>> getClassCache(ClassLoader classLoader) {
        Map<String, WeakReference<Class<?>>> cache = loaderToClassCache.get(classLoader);
        if (cache == null) {
            cache = Maps.newHashMap();
            loaderToClassCache.put(classLoader, cache);
        }

        return cache;
    }

    static synchronized Class<?> getMethodInvocationClass(ClassLoader classLoader, Method interfaceMethod)
            throws CannotCompileException {
        Map<String, WeakReference<Class<?>>> classCache = getClassCache(classLoader);
        String key = toClassCacheKey(interfaceMethod);
        WeakReference<Class<?>> invocationClassRef = classCache.get(key);
        Class<?> invocationClass;
        if (invocationClassRef == null) {
            invocationClass = createInvocationClass(classLoader, interfaceMethod);
            classCache.put(key, new WeakReference<Class<?>>(invocationClass));
        } else {
            synchronized(invocationClassRef) {
                invocationClass = invocationClassRef.get();
                if (invocationClass == null) {
                    invocationClass = createInvocationClass(classLoader, interfaceMethod);
                    classCache.put(key, new WeakReference<Class<?>>(invocationClass));
                }
            }
        }

        return invocationClass;
    }

    private static String getSimpleName(Class<?> c) {
        final String name = c.getName();
        final int ndx = name.lastIndexOf('.');
        return ndx == -1 ? name : name.substring(ndx + 1);
    }

    private static String toClassCacheKey(Method method) {
        return String.valueOf(method);
    }

    protected JavassistInvocation(Object proxy, Object target, Method method, Object[] arguments) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.arguments = ObjectUtil.defaultIfNull(ArrayUtils.clone(arguments), ProxyUtil.EMPTY_ARGUMENTS);
    }

    protected final Object getTarget() {
        return target;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getProxy() {
        return proxy;
    }
}
