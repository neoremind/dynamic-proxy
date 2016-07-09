package net.neoremind.dynamicproxy;

import net.neoremind.dynamicproxy.impl.JdkProxyCreator;

public class JdkProxyCreatorTest extends ProxyCreatorTemplateTestCase {

    @Override
    protected Class<?> getSpiImpl() {
        return JdkProxyCreator.class;
    }
}
