package net.neoremind.dynamicproxy.provider;

import net.neoremind.dynamicproxy.ObjectProvider;

/**
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-9 下午11:42:41
 */
public class ProviderDecorator<T> implements ObjectProvider<T> {

    private static final long serialVersionUID = 6927825345741706969L;

    private ObjectProvider<? extends T> inner;

    public ProviderDecorator(ObjectProvider<? extends T> inner) {
        this.inner = inner;
    }

    @Override
    public T getObject() {
        return inner.getObject();
    }

    protected ObjectProvider<? extends T> getInner() {
        return inner;
    }

    public void setInner(ObjectProvider<? extends T> inner) {
        this.inner = inner;
    }
}
