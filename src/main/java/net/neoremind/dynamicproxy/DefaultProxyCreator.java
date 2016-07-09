package net.neoremind.dynamicproxy;

import java.util.Arrays;
import java.util.ServiceLoader;

/**
 * 默认的代理创造者
 *
 * @author zhangxu
 */
public class DefaultProxyCreator implements ProxyCreator {

    public static final DefaultProxyCreator INSTANCE = new DefaultProxyCreator();

    private static final ServiceLoader<ProxyCreator> SERVICES = ServiceLoader.load(ProxyCreator.class);

    @Override
    public boolean canProxy(Class<?>... proxyClasses) {
        for (ProxyCreator proxyFactory : SERVICES) {
            if (proxyFactory.canProxy(proxyClasses)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> T createDelegatorProxy(ObjectProvider<?> delegateProvider, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        T result = (T) getCapableProxyCreator(proxyClasses).createDelegatorProxy(delegateProvider, proxyClasses);

        return result;
    }

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                      Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        T result =
                (T) getCapableProxyCreator(proxyClasses).createDelegatorProxy(classLoader, delegateProvider,
                        proxyClasses);

        return result;
    }

    @Override
    public <T> T createInterceptorProxy(Object target, Interceptor interceptor, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        T result = (T) getCapableProxyCreator(proxyClasses).createInterceptorProxy(target, interceptor, proxyClasses);

        return result;
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result =
                (T) getCapableProxyCreator(proxyClasses).createInterceptorProxy(classLoader, target, interceptor,
                        proxyClasses);

        return result;
    }

    @Override
    public <T> T createInvokerProxy(ObjectInvoker invoker, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        T result = (T) getCapableProxyCreator(proxyClasses).createInvokerProxy(invoker, proxyClasses);

        return result;
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        T result = (T) getCapableProxyCreator(proxyClasses).createInvokerProxy(classLoader, invoker, proxyClasses);

        return result;
    }

    private ProxyCreator getCapableProxyCreator(Class<?>... proxyClasses) {
        for (ProxyCreator proxyFactory : SERVICES) {
            if (proxyFactory.canProxy(proxyClasses)) {
                return proxyFactory;
            }
        }

        throw new IllegalArgumentException("Could not proxy " + Arrays.toString(proxyClasses));
    }
}
