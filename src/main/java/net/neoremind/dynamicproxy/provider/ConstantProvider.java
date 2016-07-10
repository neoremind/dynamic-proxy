package net.neoremind.dynamicproxy.provider;

import java.io.Serializable;

import net.neoremind.dynamicproxy.ObjectProvider;

/**
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午11:41:21
 */
public class ConstantProvider<T> implements ObjectProvider<T>, Serializable {

    private static final long serialVersionUID = -8677993768843722265L;

    private final T constant;

    public ConstantProvider(T constant) {
        this.constant = constant;
    }

    @Override
    public T getObject() {
        return constant;
    }
}
