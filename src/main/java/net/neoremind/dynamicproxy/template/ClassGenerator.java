package net.neoremind.dynamicproxy.template;

/**
 * 
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午10:45:03
 */
public interface ClassGenerator {

    Class<?> generateProxyClass(ClassLoader classLoader, Class<?>...proxyClasses);

}
