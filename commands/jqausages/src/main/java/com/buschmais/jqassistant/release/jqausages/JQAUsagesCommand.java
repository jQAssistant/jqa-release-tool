package com.buschmais.jqassistant.release.jqausages;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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

    private RepositoryProviderService repositorySrv;

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
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Showing the internal jQA dependencies of ",
                                               "each project", DEFAULT));

        var projects = getRepositorySrv().getProjectRepositories();

        try {
            for (ProjectRepository p : projects) {
                var line = AnsiOutput.toString(BRIGHT_YELLOW, "About to run analyse dependencies of ", BOLD,
                                               BRIGHT_YELLOW, "'", p.getName(), NORMAL, "'", DEFAULT);
                System.out.println(line);

                var pomPath = p.getHumanName() + "/pom.xml";
                var pomFile = new File(pomPath);

                try (var fis = new FileInputStream(pomFile)) {
                    var mavenReader = new MavenXpp3Reader();
                    var model = mavenReader.read(fis);
                    List<List<Dependency>> dod = new LinkedList<>();

                    var dependencies = Optional.ofNullable(model.getDependencyManagement())
                                                            .orElseGet(DependencyManagement::new)
                                                            .getDependencies();
                    var dependencies1 = Optional.ofNullable(model.getDependencies())
                                                             .orElseGet(Collections::emptyList);

                    dod.add(dependencies);
                    dod.add(dependencies1);

                    dod.stream()
                       .flatMap((Function<List<Dependency>, Stream<Dependency>>) Collection::stream)
                       .filter(d -> d.getGroupId().startsWith("com.buschmais.jqassistant"))
                       .forEach(o -> {
                           var coord = format("%s:%s:%s", o.getGroupId(), o.getArtifactId(), o.getVersion());
                           System.out.println(coord);
                       });
                }
            }
        } catch (RuntimeException e) {
            RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to show internal jQA dependencies.");
        }
    }
}
