package org.gms.exception;

public class IdTypeNotSupportedException extends Exception {
    public IdTypeNotSupportedException() {
        super("The given ID type is not supported");
    }

    public IdTypeNotSupportedException(String message) {
        super(message);
    }
}
