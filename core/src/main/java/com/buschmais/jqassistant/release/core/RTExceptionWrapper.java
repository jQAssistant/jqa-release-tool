package com.buschmais.jqassistant.release.core;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RTExceptionWrapper {
    public static BiFunction<Exception, Supplier<String>, Exception> WRAPPER = (Exception sourceException, Supplier<String> msgSupplier) -> {
        var result = sourceException;

        if (!RTException.class.isAssignableFrom(sourceException.getClass())) {
            result = new RTException(msgSupplier.get(), sourceException, false, true);
        }

        return result;
    };
}
