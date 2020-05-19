package com.buschmais.jqassistant.release.core;

public class GitUriTemplateForJQAssistant extends GitUriTemplate {
    private static final String PROJ_JQASSISTANT = "jqassistant";

    @Override
    public String getURI(String template) {
        return template.replace(PLACEHOLDER, PROJ_JQASSISTANT);
    }
}
