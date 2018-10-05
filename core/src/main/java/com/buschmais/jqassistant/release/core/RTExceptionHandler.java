package com.buschmais.jqassistant.release.core;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.ConfigurableApplicationContext;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

public class RTExceptionHandler implements SpringBootExceptionReporter {
    public RTExceptionHandler(ConfigurableApplicationContext __) {
    }

    @Override
    public boolean reportException(Throwable failure) {
        var reported = false;

        if (RTException.class.isAssignableFrom(failure.getCause().getClass())) {
            var s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                        failure.getCause().getMessage(),
                                        BOLD, AnsiColor.BRIGHT_RED, "",
                                        NORMAL);

            System.out.println(s);
            reported=true;
        }

        return reported;
    }
}
