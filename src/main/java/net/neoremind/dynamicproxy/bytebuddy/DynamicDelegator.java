package net.neoremind.dynamicproxy.bytebuddy;

import java.lang.reflect.Method;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.neoremind.dynamicproxy.ObjectInvoker;

/**
 * Byte Buddy的动态代理类
 *
 * @author zhangxu
 */
public class DynamicDelegator {

    private static ObjectInvoker invoker;

    @RuntimeType
    public static Object invoke(@This Object proxy, @Origin Method method, @AllArguments Object[] arguments)
            throws Throwable {
        return invoker.invoke(proxy, method, arguments);
    }

    public static void setInvoker(ObjectInvoker invoker) {
        DynamicDelegator.invoker = invoker;
    }
}
