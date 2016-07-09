package net.neoremind.dynamicproxy.sample;

import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.Invocation;

public class SuffixInterceptor implements Interceptor {

    private static final long serialVersionUID = 2275465822166741737L;

    private final String suffix;

    public SuffixInterceptor(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public Object intercept(Invocation methodInvocation) throws Throwable {
        Object result = methodInvocation.proceed();
        if (result instanceof String) {
            result = ((String) result) + suffix;
        }

        return result;
    }
}
