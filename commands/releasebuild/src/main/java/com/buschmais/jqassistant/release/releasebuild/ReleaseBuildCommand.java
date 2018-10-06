package com.buschmais.jqassistant.release.releasebuild;

import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenRequest;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_RED;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class ReleaseBuildCommand implements ApplicationRunner {

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
        var app = new SpringApplication(ReleaseBuildCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        var projects = getRepositorySrv().getProjectRepositories();

        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Building jQA release with execution of unit tests and with a run of jQAssistant", DEFAULT));
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Build artifacts will be uploaded to a staging repository at OSS Sonatype", DEFAULT));
        System.out.println(AnsiOutput.toString(BRIGHT_RED, "Integration tests will not be executed at the moment.", DEFAULT));
        System.out.println(AnsiOutput.toString(BRIGHT_RED, "jQAssistant constraints will not be executed at the moment.", DEFAULT));

        try {
            for (var project : projects) {
                MavenRequest request = getMavenRequest(project.getHumanName());

                String s = AnsiOutput.toString(BRIGHT_YELLOW,
                                               "About to run a release Maven build for ",
                                               BOLD, BRIGHT_YELLOW, "'",
                                               project.getName(),
                                               NORMAL, "'", DEFAULT);
                System.out.println(s);

                mavenService.doRequest(request);
            }
        } catch (Exception e) {
            RTExceptionWrapper.WRAPPER.apply(e, () -> "Release build failed");
        }
    }

    protected MavenRequest getMavenRequest(String project) {
        var properties = new Properties();

        try (var is = new FileInputStream("gpg.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var id = properties.get("gpg.keyid");
        var pp = properties.get("gpg.passphrase");
        var p1 = String.format("-Dgpg.keyname=%s", id);
        var p2 = String.format("-Dgpg.passphrase=%s", pp);
        var request = new MavenRequest();

        request.setGoals(Arrays.asList("clean", "deploy"));
        request.setParameters(List.of(p1, p2, "-Dmaven.test.failure.ignore=false",
                                      "-Djqassistant.skip=true"));
        request.setProfiles(List.of("release"));
        request.setWorkingDir(project);

        return request;
    }

}
