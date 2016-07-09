package net.neoremind.dynamicproxy;

/**
 * ProxyCreator
 *
 * @author zhangxu
 */
public interface ProxyCreator {

    boolean canProxy(Class<?>... proxyClasses);

    <T> T createDelegatorProxy(ObjectProvider<?> delegateProvider, Class<?>... proxyClasses);

    <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider, Class<?>... proxyClasses);

    <T> T createInterceptorProxy(Object target, Interceptor interceptor, Class<?>... proxyClasses);

    <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                 Class<?>... proxyClasses);

    <T> T createInvokerProxy(ObjectInvoker invoker, Class<?>... proxyClasses);

    <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses);
}
