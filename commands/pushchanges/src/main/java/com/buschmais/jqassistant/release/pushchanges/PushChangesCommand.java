package com.buschmais.jqassistant.release.pushchanges;

import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class PushChangesCommand implements ApplicationRunner {

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(PushChangesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Pushing all commits to the remote repositories.",
                                               DEFAULT));

        var remote = "gh";

        try {
            for (var repository : getRepositorySrv().getProjectRepositories()) {
                var s = AnsiOutput.toString(BRIGHT_YELLOW, "Pushing changes of ",
                                            BOLD, BRIGHT_YELLOW, "'", repository.getName(),
                                            NORMAL, BRIGHT_YELLOW, "' to '", BOLD, remote, NORMAL,
                                            "'", AnsiColor.DEFAULT);

                System.out.println(s);

                var pushResult = Git.open(new File(repository.getHumanName()))
                                    .push().setRemote(remote).setOutputStream(System.out)
                                    .setPushAll()
                                    .setPushTags()
                                    .call();
            }
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to push all changes to " + remote);
        }
    }


}
