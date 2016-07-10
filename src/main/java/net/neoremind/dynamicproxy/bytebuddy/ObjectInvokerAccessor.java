package net.neoremind.dynamicproxy.bytebuddy;

import net.neoremind.dynamicproxy.ObjectInvoker;

/**
 * {@link ObjectInvoker}的setter注入者
 *
 * @author zhangxu
 */
public interface ObjectInvokerAccessor {

    void setObjectInvoker(ObjectInvoker objectInvoker);

}
