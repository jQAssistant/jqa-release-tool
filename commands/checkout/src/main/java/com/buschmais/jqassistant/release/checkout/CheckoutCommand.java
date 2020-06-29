package com.buschmais.jqassistant.release.checkout;

import com.buschmais.jqassistant.release.core.GitUriTemplate;
import com.buschmais.jqassistant.release.core.GitUrlTemplateForJQAra;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_YELLOW;
import static org.springframework.boot.ansi.AnsiColor.DEFAULT;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class CheckoutCommand implements ApplicationRunner {

    private GitUriTemplate uriTemplate;

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setUriTemplate(GitUriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(CheckoutCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Going to checkout all needed projects", DEFAULT));

        try {
            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                var projectUri = uriTemplate.getURI(projectRepository.getRepositoryURL());
                var s = AnsiOutput.toString(BRIGHT_YELLOW,
                                            "About to checkout Git repository for project '",
                                            BOLD, projectRepository.getName(), NORMAL, BRIGHT_YELLOW,
                                            "' from '", BOLD, projectUri, NORMAL,
                                            BRIGHT_YELLOW, "'", DEFAULT);
                System.out.println(s);

                Git.cloneRepository()
                   .setURI(projectUri)
                   .setRemote("gh")
                   .setDirectory(new File(projectRepository.getHumanName()))
                   .call();
            }
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to checkout all needed projects.");
        }
    }
}
