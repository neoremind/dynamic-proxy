package net.neoremind.dynamicproxy.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;

import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.Invocation;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.template.SubclassCreatorTemplate;
import net.neoremind.dynamicproxy.util.ObjectUtil;
import net.neoremind.dynamicproxy.util.ProxyUtil;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * CglibCreator
 *
 * @author zhangxu
 */
public class CglibCreator extends SubclassCreatorTemplate {

    private static final CallbackFilter CALLBACK_FILTER = new CglibProxyFactoryCallbackFilter();

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> targetProvider,
                                      Class<?>... proxyClasses) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACK_FILTER);
        enhancer.setCallbacks(new Callback[] {new ObjectProviderDispatcher(targetProvider), new EqualsHandler(),
                new HashCodeHandler()});
        @SuppressWarnings("unchecked")
        T result = (T) enhancer.create();

        return result;
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACK_FILTER);
        enhancer.setCallbacks(new Callback[] {new InterceptorBridge(target, interceptor), new EqualsHandler(),
                new HashCodeHandler()});
        @SuppressWarnings("unchecked")
        T result = (T) enhancer.create();

        return result;
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACK_FILTER);
        enhancer.setCallbacks(new Callback[]
                {new InvokerBridge(invoker), new EqualsHandler(), new HashCodeHandler()});
        @SuppressWarnings("unchecked")
        T result = (T) enhancer.create();

        return result;
    }

    private static class CglibProxyFactoryCallbackFilter implements CallbackFilter {
        @Override
        public int accept(Method method) {
            if (ProxyUtil.isEqualsMethod(method)) {
                return 1;
            }
            if (ProxyUtil.isHashCode(method)) {
                return 2;
            }

            return 0;
        }
    }

    private static class EqualsHandler implements MethodInterceptor, Serializable {

        private static final long serialVersionUID = -6077833602011809674L;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return Boolean.valueOf(o == objects[0]);
        }
    }

    private static class HashCodeHandler implements MethodInterceptor, Serializable {

        private static final long serialVersionUID = -2918448227794094973L;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return Integer.valueOf(System.identityHashCode(o));
        }
    }

    private static class InterceptorBridge implements MethodInterceptor, Serializable {

        private static final long serialVersionUID = -3912117109550264758L;

        private final Object target;
        private final Interceptor inner;

        public InterceptorBridge(Object target, Interceptor inner) {
            this.inner = inner;
            this.target = target;
        }

        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return inner.intercept(new MethodProxyInvocation(object, target, method, args, methodProxy));
        }
    }

    private static class InvokerBridge implements net.sf.cglib.proxy.InvocationHandler, Serializable {

        private static final long serialVersionUID = 7546458626135844153L;

        private final ObjectInvoker original;

        public InvokerBridge(ObjectInvoker original) {
            this.original = original;
        }

        @Override
        public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
            return original.invoke(object, method, objects);
        }
    }

    private static class MethodProxyInvocation implements Invocation {
        private final Object proxy;
        private final Object target;
        private final Method method;
        private final Object[] args;
        private final MethodProxy methodProxy;

        public MethodProxyInvocation(Object proxy, Object target, Method method,
                                     Object[] args, MethodProxy methodProxy) {
            this.proxy = proxy;
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = ObjectUtil.defaultIfNull(ArrayUtils.clone(args), ProxyUtil.EMPTY_ARGUMENTS);
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return args;
        }

        @Override
        public Object proceed() throws Throwable {
            return methodProxy.invoke(target, args);
        }

        @Override
        public Object getProxy() {
            return proxy;
        }
    }

    private static class ObjectProviderDispatcher implements Dispatcher, Serializable {

        private static final long serialVersionUID = -3709071685921903647L;

        private final ObjectProvider<?> delegateProvider;

        public ObjectProviderDispatcher(ObjectProvider<?> delegateProvider) {
            this.delegateProvider = delegateProvider;
        }

        @Override
        public Object loadObject() {
            return delegateProvider.getObject();
        }
    }
}
