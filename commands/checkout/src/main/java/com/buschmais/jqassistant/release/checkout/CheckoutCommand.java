package com.buschmais.jqassistant.release.checkout;

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

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
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
    public void run(ApplicationArguments __) {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Going to checkout all needed projects", DEFAULT));

        try {
            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                var s = AnsiOutput.toString(BRIGHT_YELLOW,
                                            "About to checkout Git repository for project ",
                                            BOLD, BRIGHT_YELLOW, "'", projectRepository.getName(), NORMAL, BRIGHT_YELLOW,
                                            "' from '", BOLD, projectRepository.getRepositoryURL(),
                                            "'", DEFAULT);
                System.out.println(s);


                Git.cloneRepository()
                   .setURI(projectRepository.getRepositoryURL())
                   .setRemote("gh")
                   .setDirectory(new File(projectRepository.getHumanName()))
                   .call();
            }
        } catch (Exception e) {
            RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to close all needed projects.");
        }
    }
}
