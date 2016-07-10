package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;

import net.neoremind.dynamicproxy.impl.ByteBuddyCreator;
import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.sample.EchoImpl;
import net.neoremind.dynamicproxy.sample.SuffixInterceptor;

public class ByteBuddyCreatorTest extends ProxyCreatorTemplateTestCase {

    @Override
    protected Class<?> getSpiImpl() {
        return ByteBuddyCreator.class;
    }

    @Test
    public void testCanProxy() {
        assertTrue(factory.canProxy(Echo.class));
        assertTrue(factory.canProxy(EchoImpl.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanNotProxyMupltiClass() {
        assertTrue(factory.canProxy(new Class[] {Echo.class, EchoImpl.class}));
    }

    @Test
    public void testSimpleDelegator() {
        Echo proxy = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        assertEquals(proxy.echoBack("hello"), "hello");
    }

    @Test
    public void testSimpleInterceptor() {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new SuffixInterceptor("."), ECHO_ONLY);
        assertEquals(proxy.echoBack("123"), "123.");
    }

    @Test
    public void testSimpleInvoke() {
        Echo proxy = factory.createInvokerProxy(new SimpleInvoker(), ECHO_ONLY);
        assertEquals(proxy.echoBack("hello"), "hello");
    }

    // The following test cases should be refined later...

    @Test
    @Ignore
    // FIXME StackOverFlow
    public void testCreateInterceptorProxy() {
        super.testCreateInterceptorProxy();
    }

    @Test
    @Ignore
    public void testInvokerProxySerializable() throws Exception {
        super.testInvokerProxySerializable();
    }

    @Test
    @Ignore
    public void testMethodInvocationClassCaching() throws Exception {
        super.testMethodInvocationClassCaching();
    }

    @Test
    @Ignore
    public void testDelegatingProxyInterfaceOrder() {
        super.testDelegatingProxyInterfaceOrder();
    }

    @Test
    @Ignore
    public void testMethodInvocationDuplicateMethods() throws Exception {
        super.testMethodInvocationDuplicateMethods();
    }

    @Test
    @Ignore
    public void testDelegatingProxySerializable() throws Exception {
        super.testDelegatingProxySerializable();
    }

    @Test
    @Ignore
    public void testInterceptingProxySerializable() throws Exception {
        super.testInterceptingProxySerializable();
    }

    protected static class SimpleInvoker implements ObjectInvoker {

        private static final long serialVersionUID = -8586595308078627409L;

        private Object method;
        private Object[] args;
        private Object proxy;

        @Override
        public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
            return args[0];
        }
    }

}
