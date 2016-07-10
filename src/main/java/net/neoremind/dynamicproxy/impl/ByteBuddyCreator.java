package net.neoremind.dynamicproxy.impl;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

import com.google.common.base.Preconditions;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.bytebuddy.DynamicDelegator;
import net.neoremind.dynamicproxy.bytebuddy.InstanceCreator;
import net.neoremind.dynamicproxy.bytebuddy.ObjectInvokerAccessor;
import net.neoremind.dynamicproxy.exception.ProxyCreatorException;
import net.neoremind.dynamicproxy.support.AbstractInvoker;
import net.neoremind.dynamicproxy.support.DelegatorInvoker;
import net.neoremind.dynamicproxy.support.InterceptorInvoker;
import net.neoremind.dynamicproxy.support.Invokering;
import net.neoremind.dynamicproxy.template.ClassCache;
import net.neoremind.dynamicproxy.template.GeneratorTemplate;
import net.neoremind.dynamicproxy.template.SubclassCreatorTemplate;

/**
 * 使用<tt>ByteBuddy</tt>来做动态代理的{@link net.neoremind.dynamicproxy.ProxyCreator}
 *
 * @author zhangxu
 */
public class ByteBuddyCreator extends SubclassCreatorTemplate {

    protected static final ClassCache PROXY_CLASS_CACHE = new ClassCache(new ProxyGenerator());

    @Override
    public boolean canProxy(Class<?>... proxyClasses) {
        Preconditions.checkNotNull(proxyClasses, "Proxy class cannot be NULL");
        Preconditions.checkArgument(proxyClasses.length == 1,
                "ByteBuddy only supports one proxy class currently, but note that proxy class can be a Class or "
                        + "Interfaces which extends multi-interfaces");
        return super.canProxy(proxyClasses);
    }

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                      Class<?>... proxyClasses) {
        return createProxy(classLoader, new DelegatorInvoker(delegateProvider), proxyClasses);
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        return createProxy(classLoader, new InterceptorInvoker(target, interceptor), proxyClasses);
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        return createProxy(classLoader, invoker, proxyClasses);
    }

    private <T> T createProxy(ClassLoader classLoader, ObjectInvoker invoker, final Class<?>... proxyClasses) {
        canProxy(proxyClasses);
        try {
            Class<? extends T> dynamicUserType =
                    (Class<? extends T>) PROXY_CLASS_CACHE.getProxyClass(classLoader, proxyClasses);

            InstanceCreator factory;
            factory = new ByteBuddy()
                    .subclass(InstanceCreator.class)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.toConstructor(dynamicUserType))
                    .make()
                    .load(classLoader)
                    .getLoaded().newInstance();

            T ret = (T) factory.makeInstance();
            ((ObjectInvokerAccessor) ret).setObjectInvoker(invoker);
            return ret;
        } catch (Exception e) {
            throw new ProxyCreatorException("Unable to instantiate proxy from generated proxy class.", e);
        }
    }

    private static class ProxyGenerator extends GeneratorTemplate {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses) {
            Class/*<? extends Echo>*/ dynamicUserType = new ByteBuddy()
                    .subclass(proxyClasses[0])
                    .implement(ObjectInvokerAccessor.class).intercept(FieldAccessor.ofBeanProperty())
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(DynamicDelegator.class))
                    .make()
                    .load(getClass().getClassLoader())
                    .getLoaded();
            return dynamicUserType;
        }
    }

}


