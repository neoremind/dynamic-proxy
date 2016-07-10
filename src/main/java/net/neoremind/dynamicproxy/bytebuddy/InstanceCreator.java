package net.neoremind.dynamicproxy.bytebuddy;

/**
 * Byte Buddy的动态代理实例构造者
 *
 * @author zhangxu
 */
public interface InstanceCreator {

    Object makeInstance();

}
