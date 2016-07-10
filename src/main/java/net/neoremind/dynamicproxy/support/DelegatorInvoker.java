package net.neoremind.dynamicproxy.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.ObjectProvider;

/**
 * @author zhangxu
 */
public class DelegatorInvoker extends AbstractInvoker {

    private static final long serialVersionUID = -5508558789066033954L;

    private final ObjectProvider<?> delegateProvider;

    public DelegatorInvoker(ObjectProvider<?> delegateProvider) {
        this.delegateProvider = delegateProvider;
    }

    @Override
    public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegateProvider.getObject(), args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}