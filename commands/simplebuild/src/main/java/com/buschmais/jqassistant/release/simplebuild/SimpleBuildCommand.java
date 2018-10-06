package com.buschmais.jqassistant.release.simplebuild;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.ProjectVersion;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenRequest;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class SimpleBuildCommand implements ApplicationRunner {

    @Autowired
    MavenService mavenService;

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SimpleBuildCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, "Building jQA without any tests execution and " +
            "without a run of jQAssistant", AnsiColor.DEFAULT));

        try {
            for (ProjectRepository p : projects) {
                MavenRequest request = getMavenRequest(p.getHumanName());
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "About to run a simple Maven build for ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               p.getName(),
                                               NORMAL, "'", AnsiColor.DEFAULT);
                System.out.println(s);
                mavenService.doRequest(request);
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to perform a simple build for all projects.");
        }
    }

    protected MavenRequest getMavenRequest(String project) {
        MavenRequest request = new MavenRequest();

        request.setGoals(Arrays.asList("clean", "install"));
        request.setParameters(List.of("-DskipTests=true", "-Djqassistant.skip=true"));
        request.setWorkingDir(project);

        return request;
    }

}
