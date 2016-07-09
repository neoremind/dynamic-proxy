package net.neoremind.dynamicproxy;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;

import net.neoremind.dynamicproxy.util.ObjectUtil;
import net.neoremind.dynamicproxy.util.ProxyUtil;

public class MockInvocation implements Invocation {

    private final Method method;
    private final Object[] arguments;
    private final Object returnValue;

    public MockInvocation(Method method, Object returnValue, Object... arguments) {
        this.returnValue = returnValue;
        this.arguments = ObjectUtil.defaultIfNull(ArrayUtils.clone(arguments), ProxyUtil.EMPTY_ARGUMENTS);
        this.method = method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object proceed() throws Throwable {
        return returnValue;
    }
}
