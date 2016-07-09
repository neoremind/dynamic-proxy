package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import net.neoremind.dynamicproxy.exception.ProxyCreatorException;
import net.neoremind.dynamicproxy.invoke.NullInvoker;
import net.neoremind.dynamicproxy.provider.ConstantProvider;
import net.neoremind.dynamicproxy.sample.AbstractEcho;
import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.sample.EchoImpl;

public abstract class SubclassCreatorTemplateTestCase extends ProxyCreatorTemplateTestCase {

    private static final Class<?>[] DATE_ONLY = new Class[] {Date.class};

    @Override
    @Test
    public void testCanProxy() {
        assertTrue(factory.canProxy(new Class[] {Echo.class}));
        assertTrue(factory.canProxy(new Class[] {EchoImpl.class}));
        assertFalse(factory.canProxy(new Class[] {FinalEcho.class}));
        assertTrue(factory.canProxy(new Class[] {FinalMethodEcho.class, Echo.class}));
        assertFalse(factory.canProxy(new Class[] {NoDefaultConstructorEcho.class}));
        assertTrue(factory.canProxy(new Class[] {ProtectedConstructorEcho.class}));
        assertFalse(factory.canProxy(new Class[] {InvisibleEcho.class}));
        assertFalse(factory.canProxy(new Class[] {Echo.class, EchoImpl.class, String.class}));
    }

    @Override
    @Test
    public void testDelegatorEquals() throws Exception {
        EqualsEcho echo = new EqualsEcho("text");
        Echo proxy1 = factory.createDelegatorProxy(new ConstantProvider<Echo>(echo), new Class[] {EqualsEcho.class});
        Echo proxy2 = factory.createDelegatorProxy(new ConstantProvider<Echo>(echo), new Class[] {EqualsEcho.class});
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test(expected = ProxyCreatorException.class)
    public void testDelegatorWithMultipleSuperclasses() {
        factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), new Class[] {EchoImpl.class,
                String.class});
    }

    @Test
    public void testDelegatorWithSuperclass() {
        Echo echo =
                factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), new Class[] {Echo.class,
                        EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    @Override
    @Test
    public void testInterceptorEquals() {
        EqualsEcho echo = new EqualsEcho("text");
        Echo proxy1 =
                factory.createInterceptorProxy(echo, new NoOpMethodInterceptor(), new Class[] {EqualsEcho.class});
        Echo proxy2 =
                factory.createInterceptorProxy(echo, new NoOpMethodInterceptor(), new Class[] {EqualsEcho.class});
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test(expected = ProxyCreatorException.class)
    public void testInterceptorWithMultipleSuperclasses() {
        factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[] {EchoImpl.class,
                String.class});
    }

    @Test
    public void testInterceptorWithSuperclass() {
        Echo echo =
                factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[] {Echo.class,
                        EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    @Test(expected = ProxyCreatorException.class)
    public void testInvocationHandlerWithMultipleSuperclasses() {
        factory.createInvokerProxy(new NullInvoker(), new Class[] {EchoImpl.class, String.class});
    }

    @Override
    @Test
    public void testInvokerEquals() throws Exception {
        Date proxy1 = factory.createInvokerProxy(new InvokerTester(), DATE_ONLY);
        Date proxy2 = factory.createInvokerProxy(new InvokerTester(), DATE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testInvokerWithSuperclass() {
        Echo echo = factory.createInvokerProxy(new NullInvoker(), new Class[] {Echo.class, EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    @Test
    public void testProxiesWithClashingFinalMethodInSuperclass() {
        Class<?>[] proxyClasses = new Class[] {Echo.class, FinalMethodEcho.class};
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = factory.createInvokerProxy(new NullInvoker(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));
    }

    @Test
    public void testWithAbstractSuperclass() {
        Echo echo =
                factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()),
                        new Class[] {AbstractEcho.class});
        assertEquals("hello", echo.echoBack("hello"));
        assertEquals("helloworld", echo.echoBack("hello", "world"));
    }

    // -------------------------------------------------
    // Inner Classes
    // -------------------------------------------------

    public static class EqualsEcho extends EchoImpl {

        private static final long serialVersionUID = -4240253208607799194L;

        @SuppressWarnings("unused")
        private final String text;

        protected EqualsEcho() {
            this("testing");
        }

        public EqualsEcho(String text) {
            this.text = text;
        }
    }

    public static final class FinalEcho extends EchoImpl {

        private static final long serialVersionUID = -7388221136905535202L;
    }

    public static class FinalMethodEcho extends EchoImpl {

        private static final long serialVersionUID = 2806606883354415397L;

        @Override
        public final String echoBack(String message) {
            return "final";
        }
    }

    private static class InvisibleEcho extends EchoImpl {

        private static final long serialVersionUID = 6552878149325222388L;
    }

    public static class NoDefaultConstructorEcho extends EchoImpl {

        private static final long serialVersionUID = 6800249699808898408L;

        public NoDefaultConstructorEcho(String param) {

        }
    }

    public static class ProtectedConstructorEcho extends EchoImpl {

        private static final long serialVersionUID = 5332744858097002108L;

        protected ProtectedConstructorEcho() {

        }
    }
}
