package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.neoremind.dynamicproxy.provider.BeanProvider;
import net.neoremind.dynamicproxy.provider.ConstantProvider;
import net.neoremind.dynamicproxy.provider.SingletonProvider;
import net.neoremind.dynamicproxy.sample.DuplicateEcho;
import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.sample.EchoImpl;
import net.neoremind.dynamicproxy.sample.SuffixInterceptor;

public abstract class ProxyCreatorTemplateTestCase {

    protected static final Class<?>[] ECHO_ONLY = new Class[] {Echo.class};

    protected ProxyCreator factory;

    private static final Class<?>[] COMPARABLE_ONLY = new Class[] {Comparable.class};

    @BeforeClass
    public static void init() {
        final ServiceLoader<ProxyCreator> serviceLoader = ServiceLoader.load(ProxyCreator.class);
        System.out.println("Find the following implementations of ProxyCreator:");
        for (ProxyCreator proxyCreator : serviceLoader) {
            System.out.println(proxyCreator);
        }
    }

    @Before
    public void before() {
        final ServiceLoader<ProxyCreator> serviceLoader = ServiceLoader.load(ProxyCreator.class);
        for (ProxyCreator proxyCreator : serviceLoader) {
            if (proxyCreator.getClass().equals(getSpiImpl())) {
                this.factory = proxyCreator;
                System.out.println(proxyCreator + " will be tested");
                return;
            }
        }
        throw new RuntimeException("Unable to find proxy factory implementation.");
    }

    protected abstract Class<?> getSpiImpl();

    protected ObjectProvider<Echo> createSingletonEcho() {
        return new SingletonProvider<Echo>(new BeanProvider<Echo>(EchoImpl.class));
    }

    @Test
    public void testInterceptorHashCode() {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testInvokerHashCode() throws Exception {
        Echo proxy = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testDelegatorHashCode() throws Exception {
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testInterceptorEquals() {
        Date date = new Date();
        Comparable<?> proxy1 = factory.createInterceptorProxy(date, new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        Comparable<?> proxy2 = factory.createInterceptorProxy(date, new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testInvokerEquals() throws Exception {
        Comparable<?> proxy1 = factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        Comparable<?> proxy2 = factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testDelegatorEquals() throws Exception {
        Date date = new Date();
        Comparable<?> proxy1 = factory.createDelegatorProxy(new ConstantProvider<Date>(date), COMPARABLE_ONLY);
        Comparable<?> proxy2 = factory.createDelegatorProxy(new ConstantProvider<Date>(date), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testBooleanInterceptorParameter() {
        Echo echo = factory.createInterceptorProxy(new EchoImpl(), new InterceptorTester(), ECHO_ONLY);
        assertFalse(echo.echoBack(false));
        assertTrue(echo.echoBack(true));
    }

    @Test
    public void testCanProxy() {
        assertTrue(factory.canProxy(Echo.class));
        assertFalse(factory.canProxy(EchoImpl.class));
    }

    @Test
    public void testChangingArguments() {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new ChangeArgumentInterceptor(), ECHO_ONLY);
        assertEquals("something different", proxy.echoBack("whatever"));
    }

    @Test
    public void testCreateDelegatingProxy() {
        Echo echo = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        echo.echo();
        assertEquals("message", echo.echoBack("message"));
        assertEquals("ab", echo.echoBack("a", "b"));
    }

    @Test
    public void testCreateInterceptorProxy() {
        Echo target = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        Echo proxy = factory.createInterceptorProxy(target, new SuffixInterceptor(" suffix"), ECHO_ONLY);
        proxy.echo();
        assertEquals("message suffix", proxy.echoBack("message"));
    }

    @Test
    public void testDelegatingProxyClassCaching() throws Exception {
        Echo proxy1 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        Echo proxy2 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testDelegatingProxyInterfaceOrder() {
        Echo echo = factory.createDelegatorProxy(createSingletonEcho(), Echo.class, DuplicateEcho.class);
        List<Class<?>> expected =
                Lists.newLinkedList(Arrays.<Class<?>>asList(Echo.class, DuplicateEcho.class));
        List<Class<?>> actual = Lists.newLinkedList(Arrays.asList(echo.getClass().getInterfaces()));
        actual.retainAll(expected); // Doesn't alter order!
        assertEquals(expected, actual);
    }

    @Test
    public void testDelegatingProxySerializable() throws Exception {
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertSerializable(proxy);
    }

    @Test
    public void testInterceptingProxyClassCaching() throws Exception {
        Echo proxy1 = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        Echo proxy2 = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testInterceptingProxySerializable() throws Exception {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    @Test(expected = IOException.class)
    public void testInterceptorProxyWithCheckedException() throws Exception {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.ioException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterceptorProxyWithUncheckedException() throws Exception {
        Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.illegalArgument();
    }

    @Test
    public void testInterfaceHierarchies() {
        SortedSet<String> treeSet = Sets.newTreeSet();

        SortedSet<String> set =
                factory.createDelegatorProxy(new ConstantProvider<SortedSet<String>>(treeSet), SortedSet.class);

        set.add("Hello");
    }

    @Test
    public void testInvokerProxy() throws Exception {
        InvokerTester tester = new InvokerTester();
        Echo echo = factory.createInvokerProxy(tester, ECHO_ONLY);
        echo.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
        assertSame(echo, tester.proxy);
        assertNotNull(tester.args);
        assertEquals(1, tester.args.length);
        assertEquals("hello", tester.args[0]);
    }

    @Test
    public void testInvokerProxyClassCaching() throws Exception {
        Echo proxy1 = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo proxy2 = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testInvokerProxySerializable() throws Exception {
        Echo proxy = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    @Test
    public void testMethodInvocationClassCaching() throws Exception {
        InterceptorTester tester = new InterceptorTester();
        EchoImpl target = new EchoImpl();
        Echo proxy1 = factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        Echo proxy2 = factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy1.echoBack("hello1");
        Class<?> invocationClass1 = tester.invocationClass;
        proxy2.echoBack("hello2");
        assertSame(invocationClass1, tester.invocationClass);
    }

    @Test
    public void testMethodInvocationDuplicateMethods() throws Exception {
        InterceptorTester tester = new InterceptorTester();
        EchoImpl target = new EchoImpl();
        Echo proxy = factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
    }

    @Test
    public void testMethodInvocationImplementation() throws Exception {
        InterceptorTester tester = new InterceptorTester();
        EchoImpl target = new EchoImpl();
        Echo proxy = factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        proxy.echo();
        assertNotNull(tester.arguments);
        assertEquals(0, tester.arguments.length);
        assertEquals(Echo.class.getMethod("echo"), tester.method);
        assertSame(proxy, tester.proxy);
        proxy.echoBack("Hello");
        assertNotNull(tester.arguments);
        assertEquals(1, tester.arguments.length);
        assertEquals("Hello", tester.arguments[0]);
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
        proxy.echoBack("Hello", "World");
        assertNotNull(tester.arguments);
        assertEquals(2, tester.arguments.length);
        assertEquals("Hello", tester.arguments[0]);
        assertEquals("World", tester.arguments[1]);
        assertEquals(Echo.class.getMethod("echoBack", String.class, String.class), tester.method);
    }

    @Test
    public void testPrimitiveParameter() {
        Echo echo = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        assertEquals(1, echo.echoBack(1));
    }

    @Test(expected = IOException.class)
    public void testProxyWithCheckedException() throws Exception {
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        proxy.ioException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProxyWithUncheckedException() throws Exception {
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        proxy.illegalArgument();
    }

    @Test
    public void testWithNonAccessibleTargetType() {
        Echo proxy = factory.createInterceptorProxy(new PrivateEcho(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.echo();
    }

    // ****************************************************
    // Inner Classes
    // ****************************************************

    private static class ChangeArgumentInterceptor implements Interceptor {

        private static final long serialVersionUID = -6593249784975102790L;

        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable {
            methodInvocation.getArguments()[0] = "something different";
            return methodInvocation.proceed();
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
            return methodInvocation.proceed();
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
            return null;
        }
    }

    protected static class NoOpMethodInterceptor implements Interceptor, Serializable {

        private static final long serialVersionUID = 7428363767826236937L;

        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable {
            return methodInvocation.proceed();
        }
    }

    private static class PrivateEcho extends EchoImpl {

        private static final long serialVersionUID = 6128255980433219099L;
    }

    protected void assertSerializable(Object o) {
        assertTrue(o instanceof Serializable);
    }
}
