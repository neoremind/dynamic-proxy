package net.neoremind.dynamicproxy.util;

/**
 * 定义<code>Entry</code>，一般代表键值对
 */
public interface Entryable<K, V> {

    /**
     * return the key of entry
     *
     * @return key
     */
    K getKey();

    /**
     * return the value of entry
     *
     * @return value
     */
    V getValue();

}
