package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.sample.EchoImpl;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * @author zhangxu
 */
public class DefaultProxyCreatorTest {

    @Test
    public void testCanProxy() {
        ProxyCreator proxyCreator = new DefaultProxyCreator();
        assertThat(proxyCreator.canProxy(Echo.class), Matchers.is(true));
    }

    @Test
    public void testDelegator() {
        ProxyCreator proxyCreator = ProxyUtil.getInstance();
        Echo proxy = proxyCreator.createDelegatorProxy(new ObjectProvider<Object>() {
            @Override
            public Object getObject() {
                return new EchoImpl();
            }
        }, new Class<?>[] {Echo.class});
        assertEquals(proxy.echoBack("hello"), "hello");
    }

}
