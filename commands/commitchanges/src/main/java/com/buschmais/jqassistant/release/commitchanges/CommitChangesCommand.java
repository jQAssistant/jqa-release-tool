package com.buschmais.jqassistant.release.commitchanges;

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
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class CommitChangesCommand implements ApplicationRunner {

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(CommitChangesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Commiting all local changes", DEFAULT));

        try {
            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "Commiting changes in ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               projectRepository.getHumanName(),
                                               NORMAL, "'", AnsiColor.DEFAULT);
                System.out.println(s);

                Git.open(new File(projectRepository.getHumanName()))
                   .add().setUpdate(true).addFilepattern(".").call();
                Git.open(new File(projectRepository.getHumanName()))
                   .commit().setCommitter("Oliver B. Fischer", "o.b.fischer@swe-blog.net")
                   .setMessage("DAS IST EIN TEST")
                   .call();
            }
        } catch (Exception e) {
            RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to commit changes in all projects");
        }
    }
}
