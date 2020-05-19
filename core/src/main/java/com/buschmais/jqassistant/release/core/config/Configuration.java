package com.buschmais.jqassistant.release.core.config;

import com.buschmais.jqassistant.release.core.GitUriTemplate;
import com.buschmais.jqassistant.release.core.GitUriTemplateForJQAssistant;
import com.buschmais.jqassistant.release.core.GitUrlTemplateForJQAra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Autowired
    ApplicationArguments args;

    @Bean
    public GitUriTemplate createGitUriTemplate() {
        var template = args.containsOption("hot")
            ? new GitUriTemplateForJQAssistant()
            : new GitUrlTemplateForJQAra();

        return template;
    }
}
