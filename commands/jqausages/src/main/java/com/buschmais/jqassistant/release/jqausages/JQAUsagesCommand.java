package com.buschmais.jqassistant.release.jqausages;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;

import one.util.streamex.StreamEx;
import org.jboss.shrinkwrap.resolver.api.maven.*;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class JQAUsagesCommand implements ApplicationRunner {

    private static String GROUP_ID_PREFIX = "com.buschmais.jqassistant";

    private RepositoryProviderService repositorySrv;

    private Function<MavenDependency, String> toOutputFct = dependency -> {
        var combined = dependency.getGroupId() + ":" + dependency.getArtifactId();
        var version = dependency.getVersion();
        var formatted = format("%-50s : %-10s", combined, version);

        return formatted;
    };

    private Function<MavenDependency, Object> keyBuilderFct = dependency -> {
        var groupId = dependency.getGroupId();
        var artifactId = dependency.getArtifactId();
        var version = dependency.getVersion();

        return groupId + ":" + artifactId + ":" + version;
    };

    private Predicate<MavenDependency> isJQADependencyFct = dependency -> {
        return dependency.getGroupId().startsWith(GROUP_ID_PREFIX);
    };

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JQAUsagesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        String homeDir = System.getProperty("user.home");

        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Showing the internal jQA dependencies of ",
                                               "each project", DEFAULT));
        System.out.println(AnsiOutput.toString(BRIGHT_RED, "You must build everything before running this command, " +
                                               "as the values are taken from the artifacts installed in the local " +
                                               "Maven cache", DEFAULT));

        var projects = getRepositorySrv().getProjectRepositories();

        try {
            for (ProjectRepository project : projects) {
                var line = AnsiOutput.toString(BRIGHT_YELLOW, "About to run analyse dependencies of '",
                                               BOLD, project.getName(),
                                               NORMAL, BRIGHT_YELLOW, "'",
                                               DEFAULT);
                System.out.println(line);

                var pomPath = project.getHumanName() + "/pom.xml";
                var pomFile = new File(pomPath);

                MavenStrategyStage resolve = Maven.configureResolver()
                                                  .workOffline()
                                                  .fromFile(homeDir + "/jqa-release-environment/maven-settings.xml")
                                                  .loadPomFromFile(pomFile)
                                                  .importCompileAndRuntimeDependencies()
                                                  .importRuntimeAndTestDependencies()
                                                  .resolve();

                MavenWorkingSession mavenWorkingSession = ((MavenWorkingSessionContainer) resolve).getMavenWorkingSession();

                List<MavenDependency> dependencies = new ArrayList<>();
                dependencies.addAll(mavenWorkingSession.getDependenciesForResolution());
                dependencies.addAll(mavenWorkingSession.getDependencyManagement());

                StreamEx.of(dependencies)
                           .distinct(keyBuilderFct)
                           .filter(isJQADependencyFct)
                           .map(toOutputFct)
                           .sorted()
                           .forEach(System.out::println);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to show internal jQA dependencies.");
        }
    }
}
