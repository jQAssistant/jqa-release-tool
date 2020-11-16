package com.buschmais.jqassistant.release.core;

import org.springframework.stereotype.Component;

/**
 * Encapsulation of access to the environment variables used.
 *
 * The encapsulation of the acesses to environment variables
 * improves the testability of the release tooling.
 */
@Component
public class ReleaseEnvironment {
    public boolean isVariableSet(CharSequence variableName) {
        return System.getenv().containsKey(variableName);
    }

    public String getVariable(CharSequence variableName) {
        return System.getenv().get(variableName);
    }
}
