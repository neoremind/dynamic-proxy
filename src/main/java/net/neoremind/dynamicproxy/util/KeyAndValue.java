package net.neoremind.dynamicproxy.util;

/**
 * @author zhangxu
 */
public class KeyAndValue<K, V> extends Entity<KeyAndValue<K, V>> implements Entryable<K, V> {

    private static final long serialVersionUID = -4289336897863408435L;

    protected K key;

    protected V value;

    public KeyAndValue() {
    }

    public KeyAndValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return this.value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    protected Object hashKey() {
        int result = this.key == null ? 0 : 31 * this.key.hashCode();
        result = this.value == null ? 0 : this.value.hashCode() + result;
        return Integer.valueOf(result);
    }

    protected boolean isEquals(KeyAndValue<K, V> obj) {
        return this.key.equals(obj.key) && this.value.equals(obj.value);
    }
}

