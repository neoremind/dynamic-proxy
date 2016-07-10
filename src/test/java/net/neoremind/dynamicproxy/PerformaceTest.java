package net.neoremind.dynamicproxy;

import java.lang.reflect.Method;

import org.junit.Test;

import net.neoremind.dynamicproxy.impl.ASMCreator;
import net.neoremind.dynamicproxy.impl.ByteBuddyCreator;
import net.neoremind.dynamicproxy.impl.CglibCreator;
import net.neoremind.dynamicproxy.impl.JavassistCreator;
import net.neoremind.dynamicproxy.impl.JdkProxyCreator;
import net.neoremind.dynamicproxy.sample.Echo;

/**
 * @author zhangxu
 */
public class PerformaceTest {

    private static final Class<?>[] ECHO_ONLY = new Class[] {Echo.class};

    private ProxyCreator jdkProxyCreator = new JdkProxyCreator();
    private ProxyCreator javassistCreator = new JavassistCreator();
    private ProxyCreator cglibCreator = new CglibCreator();
    private ProxyCreator asmCreator = new ASMCreator();
    private ProxyCreator byteBuddyCreator = new ByteBuddyCreator();

    public static final int INVOKE_NUM = 1000 * 1000 * 1;

    @Test
    public void testPerformance() {
        Echo jdkProxyCreatorInvokerProxy = jdkProxyCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo javassistCreatorInvokerProxy = javassistCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo cglibCreatorInvokerProxy = cglibCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo asmCreatorInvokerProxy = asmCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo byteBuddyCreatorInvokerProxy = byteBuddyCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);

        innerTest(byteBuddyCreatorInvokerProxy);
        innerTest(asmCreatorInvokerProxy);
        //innerTest(cglibCreatorInvokerProxy);
       // innerTest(javassistCreatorInvokerProxy);
       // innerTest(jdkProxyCreatorInvokerProxy);
    }

    public void innerTest(Echo echo) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < INVOKE_NUM; i++) {
            echo.echoBack("abc");
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
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
            return (String) args[0];
        }
    }

}
