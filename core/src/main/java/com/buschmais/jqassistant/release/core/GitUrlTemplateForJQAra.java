package com.buschmais.jqassistant.release.core;

public class GitUrlTemplateForJQAra extends GitUriTemplate {
    private static final String PROJ_JQARA = "jqara";

    @Override
    public String getURI(String template) {
        return template.replace(PLACEHOLDER, PROJ_JQARA);
    }
}
