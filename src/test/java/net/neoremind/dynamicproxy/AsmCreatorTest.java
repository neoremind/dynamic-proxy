package net.neoremind.dynamicproxy;

import net.neoremind.dynamicproxy.impl.ASMCreator;

public class AsmCreatorTest extends SubclassCreatorTemplateTestCase {

    @Override
    protected Class<?> getSpiImpl() {
        return ASMCreator.class;
    }
}
