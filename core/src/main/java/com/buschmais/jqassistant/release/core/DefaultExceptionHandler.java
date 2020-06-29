package com.buschmais.jqassistant.release.core;

import java.util.Optional;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.ConfigurableApplicationContext;

import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

public class DefaultExceptionHandler implements SpringBootExceptionReporter {
    public DefaultExceptionHandler(ConfigurableApplicationContext __) {
    }

    @Override
    public boolean reportException(Throwable failure) {

        Optional.ofNullable(failure.getCause()).ifPresent(cause -> {
            var error = AnsiOutput.toString(AnsiColor.RED, cause.getMessage(),
                                            NORMAL, AnsiColor.DEFAULT);

            System.out.println(error);
        });

        System.out.println("---------------");
        System.out.println(failure);

        var message = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                          "Internal error occurred.",
                                          NORMAL, AnsiColor.DEFAULT);

        System.out.println(message);
        System.out.println(failure.getMessage());

        return true;
    }
}
