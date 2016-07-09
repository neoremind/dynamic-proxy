package net.neoremind.dynamicproxy.provider;

import java.io.Serializable;

import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.util.ReflectionUtil;

public class BeanProvider<T> implements ObjectProvider<T>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6261791550469264285L;

    private final Class<? extends T> beanClass;

    public BeanProvider(Class<? extends T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public T getObject() {
        return ReflectionUtil.newInstance(beanClass);
    }
}
