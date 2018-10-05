package com.buschmais.jqassistant.release.core;

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

        var s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                    "Internal error occured.",
                                    NORMAL, AnsiColor.DEFAULT);

        System.out.println(s);
        System.out.println(failure.getMessage());

        return true;
    }
}
