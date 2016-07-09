package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.sample.EchoImpl;
import net.neoremind.dynamicproxy.sample.Sound;

/**
 * @author zhangxu
 */
public class ProxyCreatorSpiTest {

    private static final Class<?>[] ECHO_ONLY = new Class[] {Echo.class};

    protected ProxyCreator factory;

    @Before
    public void init() {
        final ServiceLoader<ProxyCreator> serviceLoader = ServiceLoader.load(ProxyCreator.class);
        Iterator<ProxyCreator> iter = serviceLoader.iterator();
        if (iter.hasNext()) {
            this.factory = iter.next();
        } else {
            throw new RuntimeException("Unable to find proxy factory implementation.");
        }
    }

    @Test
    public void testDelegator() throws Exception {
        Echo echoProxy = factory.createDelegatorProxy(new DecoratorProvider(new EchoImpl()), Echo.class);
        System.out.println(echoProxy.echoBack("hello"));
        assertThat(echoProxy.echoBack("hello"), Matchers.is("hello.Huh!"));
    }

    @Test
    public void testInterceptor() throws Exception {
        Echo echoProxy = factory.createInterceptorProxy(new EchoImpl(), new InterceptorTester(), ECHO_ONLY);
        System.out.println(echoProxy.echoBack("hello"));
        System.out.println(echoProxy.echoSound(10));
        assertThat(echoProxy.echoBack("hello"), Matchers.is("hello"));
        assertThat(echoProxy.echoSound(10), Matchers.is(new Sound(10)));
    }

    @Test
    public void testInvoker() throws Exception {
        Echo echoProxy = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        System.out.println(echoProxy.echoBack("hello"));
        System.out.println(echoProxy.echoSound(10));
        assertThat(echoProxy.echoBack("hello"), Matchers.is("This is test! Huh!"));
        assertThat(echoProxy.echoSound(10), Matchers.is(new Sound(10)));
    }

    public class DecoratorProvider implements ObjectProvider<Echo> {

        private final Echo delegate;

        public DecoratorProvider(Echo delegate) {
            this.delegate = delegate;
        }

        @Override
        public Echo getObject() {
            return new AppenderEchoImpl(delegate);
        }
    }

    public class AppenderEchoImpl extends EchoImpl implements Echo {

        private Echo echo;

        public AppenderEchoImpl(Echo echo) {
            this.echo = echo;
        }

        @Override
        public String echoBack(String messages) {
            return super.echoBack(messages) + ".Huh!";
        }

        public Echo getEcho() {
            return echo;
        }

        public void setEcho(Echo echo) {
            this.echo = echo;
        }
    }

    protected static class InterceptorTester implements Interceptor {

        private static final long serialVersionUID = 6266374153664803147L;

        private Object[] arguments;
        private Method method;
        private Object proxy;
        private Class<?> invocationClass;

        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable {
            arguments = methodInvocation.getArguments();
            method = methodInvocation.getMethod();
            proxy = methodInvocation.getProxy();
            invocationClass = methodInvocation.getClass();

            System.out.println("Before invoke");
            Object ret = methodInvocation.proceed();
            System.out.println("After invoke");
            return ret;
        }
    }

    protected static class InvokerTester implements ObjectInvoker {

        private static final long serialVersionUID = -8586595308078627409L;

        private Object method;
        private Object[] args;
        private Object proxy;

        @Override
        public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
            System.out.println(method.getName());
            if (method.getName().equals("echoSound")) {
                return new Sound((Integer) args[0]);
            }
            return "This is test! Huh!";
        }
    }
}
