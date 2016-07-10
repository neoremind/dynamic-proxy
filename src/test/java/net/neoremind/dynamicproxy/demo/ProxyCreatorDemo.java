package net.neoremind.dynamicproxy.demo;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.hamcrest.Matchers;

import net.neoremind.dynamicproxy.DefaultProxyCreator;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ProxyCreator;
import net.neoremind.dynamicproxy.impl.CglibCreator;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * @author zhangxu
 */
public class ProxyCreatorDemo {

    public static void main(String[] args) {
        testNewSpecificCreator();
        testSpiCreator();
    }

    public static void testNewSpecificCreator() {
        ProxyCreator proxyCreator = new CglibCreator();
        ObjectInvoker fixedStringObjInvoker = new ObjectInvoker() {
            @Override
            public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
                return "Hello world!";
            }
        };
        EchoService service = proxyCreator.createInvokerProxy(fixedStringObjInvoker, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("Hello world!"));
    }

    public static void testSpiCreator() {
        ProxyCreator proxyCreator = ProxyUtil.getInstance();
        EchoService service = proxyCreator.createInvokerProxy(new ObjectInvoker() {
            @Override
            public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
                return arguments[0];
            }
        }, EchoService.class);
        assertThat(service.echo("wow"), Matchers.is("wow"));
    }

}
