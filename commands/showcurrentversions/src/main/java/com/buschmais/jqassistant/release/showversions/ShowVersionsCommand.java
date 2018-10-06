package com.buschmais.jqassistant.release.showversions;

import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiColor.DEFAULT;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository"
})
public class ShowVersionsCommand implements ApplicationRunner {

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(ShowVersionsCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Showing the current version of all projects", DEFAULT));

        try {
            var projects = getRepositorySrv().getProjectRepositories();

            for (var project : projects) {
                var path = project.getHumanName() + "/pom.xml";

                try (var fis = new FileInputStream(path)) {
                    var reader = new MavenXpp3Reader();
                    var model = reader.read(fis);
                    var line = format("%-40s\t%s", model.getName(), model.getVersion());
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to show the version of all projects.");
        }
    }
}
