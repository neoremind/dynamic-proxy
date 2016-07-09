package net.neoremind.dynamicproxy;

import net.neoremind.dynamicproxy.impl.CglibCreator;

public class CglibCreatorTest extends SubclassCreatorTemplateTestCase {

    @Override
    protected Class<?> getSpiImpl() {
        return CglibCreator.class;
    }
}
