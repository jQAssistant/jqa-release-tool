package com.buschmais.jqassistant.release.erase;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository"
})
public class EraseCommand implements ApplicationRunner {

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(EraseCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN,
                                               "Deleting all jQA repositories from your disk",
                                               DEFAULT));

        try {
            for (var repository : getRepositorySrv().getProjectRepositories()) {
                var humanName = repository.getHumanName();
                var projectDirectory = new File(humanName);

                String s = AnsiOutput.toString(BRIGHT_YELLOW, "Deleting repository ", BOLD, BRIGHT_YELLOW, "'",
                                               projectDirectory.getName(), NORMAL, "'", DEFAULT);
                System.out.println(s);

                if (projectDirectory.exists()) {
                    Files.walk(projectDirectory.toPath())
                         .sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(File::delete);
                }
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to delete all project directories");
        }
    }
}
