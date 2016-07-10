package net.neoremind.dynamicproxy.template;

/**
 *
 * @author zhangxu
 */
public interface ClassGenerator {

    Class<?> generateProxyClass(ClassLoader classLoader, Class<?>...proxyClasses);

}
