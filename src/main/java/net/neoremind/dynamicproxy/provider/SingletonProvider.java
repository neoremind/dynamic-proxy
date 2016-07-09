package net.neoremind.dynamicproxy.provider;

import net.neoremind.dynamicproxy.ObjectProvider;

/**
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午11:43:10
 */
public class SingletonProvider<T> extends ProviderDecorator<T> {

    /**
     *
     */
    private static final long serialVersionUID = -3715549852313953689L;

    private T instance;

    public SingletonProvider(ObjectProvider<? extends T> inner) {
        super(inner);

        instance = super.getObject();
        setInner(null);
    }

    @Override
    public T getObject() {
        return instance;
    }
}
