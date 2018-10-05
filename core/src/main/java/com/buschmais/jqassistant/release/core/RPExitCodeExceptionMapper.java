package com.buschmais.jqassistant.release.core;

import org.springframework.boot.ExitCodeExceptionMapper;

public class RPExitCodeExceptionMapper implements ExitCodeExceptionMapper {
    public static final int INTERNAL_ERROR_EXIT_CODE = 10;
    public static final int OTHER_ERROR_EXIT_CODE = 1;

    @Override
    public int getExitCode(Throwable exception) {
        var exitCode = OTHER_ERROR_EXIT_CODE;

        if (RTException.class.isAssignableFrom(exception.getClass())) {
            exitCode = INTERNAL_ERROR_EXIT_CODE;
        }

        return exitCode;
    }
}
