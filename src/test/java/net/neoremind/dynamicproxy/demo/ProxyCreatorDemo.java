package net.neoremind.dynamicproxy.demo;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.hamcrest.Matchers;

import net.neoremind.dynamicproxy.DefaultProxyCreator;
import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.Invocation;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.ProxyCreator;
import net.neoremind.dynamicproxy.impl.ASMCreator;
import net.neoremind.dynamicproxy.impl.ByteBuddyCreator;
import net.neoremind.dynamicproxy.impl.CglibCreator;
import net.neoremind.dynamicproxy.impl.JavassistCreator;
import net.neoremind.dynamicproxy.impl.JdkProxyCreator;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * 用于github上的wiki
 *
 * @author zhangxu
 */
public class ProxyCreatorDemo {

    public static void main(String[] args) {
        new ProxyCreatorDemo().testNewSpecificCreatorInvoke();
        new ProxyCreatorDemo().testSpiCreatorInvoke();
        new ProxyCreatorDemo().testSpiCreatorInterceptor();
        new ProxyCreatorDemo().testSpiCreatorDelegator();
    }

    public void testNewSpecificCreatorInvoke() {
        ProxyCreator proxyCreator = new CglibCreator();
        //        ProxyCreator proxyCreator = new ASMCreator();
        //        ProxyCreator proxyCreator = new JavassistCreator();
        //        ProxyCreator proxyCreator = new ByteBuddyCreator();
        //        ProxyCreator proxyCreator = new JdkProxyCreator();
        ObjectInvoker fixedStringObjInvoker = new ObjectInvoker() {
            @Override
            public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
                return "Hello world!";
            }
        };
        EchoService service = proxyCreator.createInvokerProxy(fixedStringObjInvoker, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("Hello world!"));
    }

    public void testSpiCreatorInvoke() {
        ProxyCreator proxyCreator = ProxyUtil.getInstance();
        EchoService service = proxyCreator.createInvokerProxy(new ObjectInvoker() {
            @Override
            public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
                return arguments[0];
            }
        }, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("wow"));
    }

    public void testSpiCreatorInterceptor() {
        ProxyCreator proxyCreator = new DefaultProxyCreator();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                return invocation.proceed() + ".Huh!";
            }
        };
        EchoService service =
                proxyCreator.createInterceptorProxy(new EchoServiceImpl(), interceptor, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("wow.Huh!"));
    }

    public void testSpiCreatorDelegator() {
        ProxyCreator proxyCreator = new DefaultProxyCreator();
        ObjectProvider objectProvider = new ObjectProvider() {

            @Override
            public Object getObject() {
                return new DecoratorEchoService(new EchoServiceImpl());
            }
        };
        EchoService service =
                proxyCreator.createDelegatorProxy(objectProvider, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("WOW"));
    }

    class DecoratorEchoService implements EchoService {

        private EchoService delegate;

        public DecoratorEchoService(EchoService delegate) {
            this.delegate = delegate;
        }

        @Override
        public String echo(String message) {
            message = message.toUpperCase();
            return delegate.echo(message);
        }

        public EchoService getDelegate() {
            return delegate;
        }

        public void setDelegate(EchoService delegate) {
            this.delegate = delegate;
        }
    }

}
