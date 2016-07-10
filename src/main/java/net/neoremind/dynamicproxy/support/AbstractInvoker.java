package net.neoremind.dynamicproxy.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * @author zhangxu
 */
public abstract class AbstractInvoker implements ObjectInvoker, Serializable {

    private static final long serialVersionUID = -4379566817117145667L;

    @Override
    public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
        if (ProxyUtil.isHashCode(method)) {
            return Integer.valueOf(System.identityHashCode(proxy));
        }

        if (ProxyUtil.isEqualsMethod(method)) {
            return Boolean.valueOf(proxy == args[0]);
        }

        return invokeImpl(proxy, method, args);
    }

    public abstract Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable;
}
