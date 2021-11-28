package com.buschmais.jqassistant.release.tagrelease;

import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class ResetCommand implements ApplicationRunner {
    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(ResetCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Resetting all repositories", DEFAULT));

        try {
            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                var git = Git.open(new File(projectRepository.getHumanName()));

                git.reset()
                   .setMode(ResetType.HARD)
                   .setRef("HEAD")
                   .call();

                var msg = AnsiOutput.toString(BRIGHT_YELLOW, "Resetted '",
                                              BOLD, BRIGHT_YELLOW, projectRepository.getName(),
                                              NORMAL, BRIGHT_YELLOW, "' back to '",
                                              BOLD, BRIGHT_YELLOW, "HEAD",
                                              NORMAL, BRIGHT_WHITE, "'", DEFAULT);

                System.out.println(msg);
            }
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to reset all projects");
        }
    }
}
