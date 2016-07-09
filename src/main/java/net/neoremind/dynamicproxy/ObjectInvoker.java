package net.neoremind.dynamicproxy;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface ObjectInvoker extends Serializable {

    Object invoke(Object proxy, Method method, Object... arguments) throws Throwable;

}
