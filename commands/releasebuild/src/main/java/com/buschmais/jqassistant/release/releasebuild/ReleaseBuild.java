package com.buschmais.jqassistant.release.releasebuild;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class ReleaseBuild implements CommandLineRunner {

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
        SpringApplication app = new SpringApplication(ReleaseBuild.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        Properties properties = new Properties();

        try (var is = new FileInputStream("gpg.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            for (ProjectRepository p : projects) {
                MavenRequest request = getMavenRequest(p.getHumanName());

                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "About to run a release Maven build for ",
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
        Properties properties = new Properties();

        try (var is = new FileInputStream("gpg.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var id = properties.get("gpg.keyid");
        var pp = properties.get("gpg.passphrase");
        var p1 = String.format("-Dgpg.keyname=%s", id);
        var p2 = String.format("-Dgpg.passphrase=%s", pp);

        MavenRequest request = new MavenRequest();

        request.setGoals(Arrays.asList("clean", "install", "deploy"));
        request.setParameters(List.of(p1, p2, "-Dmaven.test.failure.ignore=false",
                                      "-Djqassistant.skip=true"));
        request.setProfiles(List.of("release"));
        request.setWorkingDir(project);

        return request;
    }

}
