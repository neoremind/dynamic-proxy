package net.neoremind.dynamicproxy.template;

import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.ProxyCreator;

/**
 * CreatorTemplate
 *
 * @author zhangxu
 */
public abstract class CreatorTemplate implements ProxyCreator {

    @Override
    public boolean canProxy(Class<?>... proxyClasses) {
        for (Class<?> proxyClass : proxyClasses) {
            if (!proxyClass.isInterface()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <T> T createDelegatorProxy(ObjectProvider<?> delegateProvider, Class<?>... proxyClasses) {
        return createDelegatorProxy(Thread.currentThread().getContextClassLoader(), delegateProvider, proxyClasses);
    }

    @Override
    public <T> T createInterceptorProxy(Object target, Interceptor interceptor, Class<?>... proxyClasses) {
        return createInterceptorProxy(Thread.currentThread().getContextClassLoader(),
                target, interceptor, proxyClasses);
    }

    @Override
    public <T> T createInvokerProxy(ObjectInvoker invoker, Class<?>... proxyClasses) {
        return createInvokerProxy(Thread.currentThread().getContextClassLoader(), invoker, proxyClasses);
    }

}
