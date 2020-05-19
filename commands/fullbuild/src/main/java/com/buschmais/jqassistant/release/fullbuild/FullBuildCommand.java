package com.buschmais.jqassistant.release.fullbuild;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class FullBuildCommand implements CommandLineRunner {

    @Autowired
    private MavenService mavenService;

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FullBuildCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, "Building jQA with execution of unit tests and with a run of jQAssistant", AnsiColor.DEFAULT));
        System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_RED, "Integration tests will not be executed at the moment.", AnsiColor.DEFAULT));


        try {
            for (ProjectRepository p : projects) {
                var request = getMavenRequest(p.getHumanName());
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "About to run a full Maven build for ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               p.getName(),
                                               NORMAL, "'", AnsiColor.DEFAULT);
                System.out.println(s);



                mavenService.doRequest(request);
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Release build failed");
        }
    }

    protected MavenRequest getMavenRequest(String project) {
        MavenRequest request = new MavenRequest();

        request.setWorkingDir(project);
        request.setParameters(List.of("-Dmaven.test.failure.ignore=false"));
        request.setGoals(List.of("clean", "install"));

        return request;
    }

}
