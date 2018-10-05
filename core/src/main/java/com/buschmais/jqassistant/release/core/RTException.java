package com.buschmais.jqassistant.release.core;

public class RTException extends RuntimeException {
    public RTException(String message) {
        super(message);
    }

    public RTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
