package net.neoremind.dynamicproxy;

import net.neoremind.dynamicproxy.impl.JavassistCreator;

public class JavassistCreatorTest extends SubclassCreatorTemplateTestCase {

    @Override
    protected Class<?> getSpiImpl() {
        return JavassistCreator.class;
    }
}
