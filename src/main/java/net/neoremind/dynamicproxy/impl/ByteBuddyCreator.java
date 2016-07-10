package net.neoremind.dynamicproxy.impl;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

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
import net.neoremind.dynamicproxy.template.SubclassCreatorTemplate;

/**
 * 使用<tt>ByteBuddy</tt>来做动态代理的{@link net.neoremind.dynamicproxy.ProxyCreator}
 *
 * @author zhangxu
 */
public class ByteBuddyCreator extends SubclassCreatorTemplate {

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                      Class<?>... proxyClasses) {
        return null;
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        return null;
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        Class/*<? extends Echo>*/ dynamicUserType = new ByteBuddy()
                .subclass(proxyClasses[0])
                .implement(ObjectInvokerAccessor.class).intercept(FieldAccessor.ofBeanProperty())
                .method(not(isDeclaredBy(Object.class)))
                .intercept(MethodDelegation.to(DynamicDelegator.class))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        InstanceCreator factory = null;
        try {
            factory = new ByteBuddy()
                    .subclass(InstanceCreator.class)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.toConstructor(dynamicUserType))
                    .make()
                    .load(dynamicUserType.getClassLoader())
                    .getLoaded().newInstance();
        } catch (InstantiationException e) {
            throw new ProxyCreatorException("Failed to build Instance Creator", e);
        } catch (IllegalAccessException e) {
            throw new ProxyCreatorException("Failed to build Instance Creator", e);
        }

        T ret = (T) factory.makeInstance();
        ((ObjectInvokerAccessor) ret).setObjectInvoker(invoker);
        return ret;
    }
}
