package net.neoremind.dynamicproxy.util;

import java.io.Serializable;

/**
 * @author zhangxu
 */
public abstract class Entity<T extends Entity<T>> implements Serializable {

    private static final long serialVersionUID = -3365154083759739647L;

    public Entity() {
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            T other = (T) obj;
            return this.isEquals(other);
        } else {
            return false;
        }
    }

    protected abstract boolean isEquals(T var1);

    public int hashCode() {
        if (this.hashKey() == null) {
            return super.hashCode();
        } else {
            byte result = 1;
            int result1 = 31 * result + this.hashKey().hashCode();
            return result1;
        }
    }

    protected abstract Object hashKey();
}

