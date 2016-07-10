package net.neoremind.dynamicproxy.support;

import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.Interceptor;

/**
 * @author zhangxu
 */
public class InterceptorInvoker extends AbstractInvoker {

    private static final long serialVersionUID = -1409839456330694048L;

    private final Object target;
    private final Interceptor methodInterceptor;

    public InterceptorInvoker(Object target, Interceptor methodInterceptor) {
        this.target = target;
        this.methodInterceptor = methodInterceptor;
    }

    @Override
    public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable {
        final ReflectionInvocation invocation = new ReflectionInvocation(target, proxy, method, args);
        return methodInterceptor.intercept(invocation);
    }
}
