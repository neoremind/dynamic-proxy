package net.neoremind.dynamicproxy.support;

import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.ObjectInvoker;

/**
 * @author zhangxu
 */
public class Invokering extends AbstractInvoker {

    private static final long serialVersionUID = 1822915849692440651L;

    private final ObjectInvoker invoker;

    public Invokering(ObjectInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(proxy, method, args);
    }
}
