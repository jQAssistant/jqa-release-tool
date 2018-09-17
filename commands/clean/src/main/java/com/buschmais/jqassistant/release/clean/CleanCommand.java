package com.buschmais.jqassistant.release.clean;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenRequest;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class CleanCommand implements CommandLineRunner {

    private RepositoryProviderService repositorySrv;
    private MavenService mavenService;

    public MavenService getMavenService() {
        return mavenService;
    }

    @Autowired
    public void setMavenService(MavenService service) {
        this.mavenService = service;
    }

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CleanCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, "Cleaning the build", AnsiColor.DEFAULT));

        try {
            for (ProjectRepository p : projects) {
                MavenRequest request = getMavenRequest(p.getHumanName());

                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "About to run a Maven clean for ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               p.getName(),
                                               NORMAL, "'", AnsiColor.DEFAULT);
                System.out.println(s);

                mavenService.doRequest(request);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected MavenRequest getMavenRequest(String project) {
        MavenRequest request = new MavenRequest();

        request.setGoals(Arrays.asList("clean"));
        request.setWorkingDir(project);

        return request;
    }

}
