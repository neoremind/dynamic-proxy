package net.neoremind.dynamicproxy.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;

import net.neoremind.dynamicproxy.Invocation;
import net.neoremind.dynamicproxy.util.ObjectUtil;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * @author zhangxu
 */
public class ReflectionInvocation implements Invocation {
    private final Method method;
    private final Object[] arguments;
    private final Object proxy;
    private final Object target;

    public ReflectionInvocation(final Object target, final Object proxy, final Method method,
                                final Object[] arguments) {
        this.method = method;
        this.arguments = ObjectUtil.defaultIfNull(ArrayUtils.clone(arguments), ProxyUtil.EMPTY_ARGUMENTS);
        this.proxy = proxy;
        this.target = target;
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

    @Override
    public Object proceed() throws Throwable {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
