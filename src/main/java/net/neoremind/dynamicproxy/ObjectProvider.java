package net.neoremind.dynamicproxy;

import java.io.Serializable;

public interface ObjectProvider<T> extends Serializable {

    T getObject();
}
