package net.neoremind.dynamicproxy.invoke;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.neoremind.dynamicproxy.ObjectInvoker;

/**
 * NoneInvoker
 *
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-4-2 下午11:03:28
 */
public class NoneInvoker implements ObjectInvoker, Serializable {

    private static final long serialVersionUID = -6264105341970426080L;

    @Override
    public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
        return null;
    }

}
