package net.neoremind.dynamicproxy.exception;

public class ProxyCreatorException extends RuntimeException {

    private static final long serialVersionUID = -5278506190465957728L;

    public ProxyCreatorException() {
    }

    public ProxyCreatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyCreatorException(String message) {
        super(message);
    }

    public ProxyCreatorException(Throwable cause) {
        super(cause);
    }

}
