package com.buschmais.jqassistant.release.clean;

import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenRequest;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class CleanCommand implements ApplicationRunner {

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
    public void run(ApplicationArguments __) throws Exception {
        var projects = getRepositorySrv().getProjectRepositories();

        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Cleaning the Maven build of all projects", DEFAULT));

        try {
            for (var project : projects) {
                var request = getMavenRequest(project.getHumanName());

                var s = AnsiOutput.toString(BRIGHT_YELLOW, "About to run a Maven clean for ",
                                            BOLD, BRIGHT_YELLOW, "'", project.getName(), NORMAL, "'", DEFAULT);
                System.out.println(s);

                mavenService.doRequest(request);
            }
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to clean all Maven builds");
        }
    }

    protected MavenRequest getMavenRequest(String project) {
        var request = new MavenRequest();

        request.setGoals(Arrays.asList("clean"));
        request.setWorkingDir(project);

        return request;
    }

}
