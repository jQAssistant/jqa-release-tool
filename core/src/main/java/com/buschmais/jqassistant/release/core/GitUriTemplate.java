package com.buschmais.jqassistant.release.core;

public abstract class GitUriTemplate {
    protected static final String PLACEHOLDER = "{project}";

    public abstract String getURI(String template);
}
