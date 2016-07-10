package net.neoremind.dynamicproxy;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import net.neoremind.dynamicproxy.impl.ASMCreator;
import net.neoremind.dynamicproxy.impl.ByteBuddyCreator;
import net.neoremind.dynamicproxy.impl.CglibCreator;
import net.neoremind.dynamicproxy.impl.JavassistCreator;
import net.neoremind.dynamicproxy.impl.JdkProxyCreator;
import net.neoremind.dynamicproxy.sample.Echo;

/**
 * 性能测试JVM参数设置
 * <pre>
 *     -Xms512m -Xmx512m -XX:PermSize=128M -XX:MaxPermSize=128M
 * </pre>
 *
 * @author zhangxu
 */
public class PerformaceTest {

    private static final Class<?>[] ECHO_ONLY = new Class[] {Echo.class};

    private ProxyCreator jdkProxyCreator = new JdkProxyCreator();
    private ProxyCreator javassistCreator = new JavassistCreator();
    private ProxyCreator cglibCreator = new CglibCreator();
    private ProxyCreator asmCreator = new ASMCreator();
    private ProxyCreator byteBuddyCreator = new ByteBuddyCreator();

    @Test
    public void testCreatePerformance() {
        testCreate(10000);
    }

    @Test
    public void testInvokePerformance() {
        testInvoke(1000 * 1000 * 1);
    }

    public void testCreate(int invokeNum) {
        List<Profiler> profilerList = Lists.newArrayList();

        profilerList.add(innerTestCreate(byteBuddyCreator, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestCreate(asmCreator, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestCreate(cglibCreator, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestCreate(javassistCreator, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestCreate(jdkProxyCreator, invokeNum));

        System.out.println("-------------------------------------");
        System.out.println(String.format("| Create proxy number:%8d      |", invokeNum));
        System.out.println("-------------------------------------");
        System.out.println("|   ProxyCreator   |    time cost   |");
        System.out.println("-------------------------------------");
        for (Profiler profiler : profilerList) {
            System.out.println(profiler);

        }
        System.out.println("-------------------------------------");
    }

    public void testInvoke(int invokeNum) {
        List<Profiler> profilerList = Lists.newArrayList();
        Echo jdkProxyCreatorInvokerProxy = jdkProxyCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo javassistCreatorInvokerProxy = javassistCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo cglibCreatorInvokerProxy = cglibCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo asmCreatorInvokerProxy = asmCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        Echo byteBuddyCreatorInvokerProxy = byteBuddyCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);

        tryGcAndSleep(2);

        profilerList.add(innerTestInvoke(byteBuddyCreator, byteBuddyCreatorInvokerProxy, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestInvoke(asmCreator, asmCreatorInvokerProxy, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestInvoke(cglibCreator, cglibCreatorInvokerProxy, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestInvoke(javassistCreator, javassistCreatorInvokerProxy, invokeNum));
        tryGcAndSleep(2);
        profilerList.add(innerTestInvoke(jdkProxyCreator, jdkProxyCreatorInvokerProxy, invokeNum));

        System.out.println("-------------------------------------");
        System.out.println(String.format("| Invoke number:%12d        |", invokeNum));
        System.out.println("-------------------------------------");
        System.out.println("|   ProxyCreator   |    time cost   |");
        System.out.println("-------------------------------------");
        for (Profiler profiler : profilerList) {
            System.out.println(profiler);

        }
        System.out.println("-------------------------------------");
    }

    public Profiler innerTestCreate(ProxyCreator proxyCreator, int invokeNum) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < invokeNum; i++) {
            proxyCreator.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        }
        return Profiler.apply(System.currentTimeMillis(), start)
                .setProxyCreatorName(proxyCreator.getClass().getSimpleName());
    }

    public Profiler innerTestInvoke(ProxyCreator proxyCreator, Echo echo, int invokeNum) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < invokeNum; i++) {
            echo.echoBack("abc");
        }
        return Profiler.apply(System.currentTimeMillis(), start)
                .setProxyCreatorName(proxyCreator.getClass().getSimpleName());
    }

    private static class Profiler {

        private String proxyCreatorName;

        private long timeCost;

        public Profiler(long timeCost) {
            this.timeCost = timeCost;
        }

        public static Profiler apply(long end, long start) {
            return new Profiler(end - start);
        }

        public Profiler setProxyCreatorName(String proxyCreatorName) {
            this.proxyCreatorName = proxyCreatorName;
            return this;
        }

        public String getProxyCreatorName() {
            return proxyCreatorName;
        }

        public long getTimeCost() {
            return timeCost;
        }

        @Override
        public String toString() {
            return String.format("| %16s |   %5dms      |", proxyCreatorName, timeCost);
        }
    }

    private void tryGcAndSleep(int sec) {
        try {
            System.gc();
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static class InvokerTester implements ObjectInvoker {

        private static final long serialVersionUID = -8586595308078627409L;

        @Override
        public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
            return args[0];
        }
    }

}
