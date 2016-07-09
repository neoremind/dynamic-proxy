package net.neoremind.dynamicproxy;

import java.io.Serializable;

/**
 * 定义拦截器
 *
 * @author zhangxu
 */
public interface Interceptor extends Serializable {

    Object intercept(Invocation invocation) throws Throwable;

}
