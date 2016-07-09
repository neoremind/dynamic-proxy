package net.neoremind.dynamicproxy.invoke;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * 代表“NULL”的Invoker
 *
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-10 上午12:51:56
 */
public class NullInvoker implements ObjectInvoker, Serializable {

    private static final long serialVersionUID = 4430908314204545174L;

    public static final NullInvoker INSTANCE = new NullInvoker();

    @Override
    public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
        Class<?> returnType = method.getReturnType();

        return ProxyUtil.nullValue(returnType);
    }
}
