package com.buschmais.jqassistant.release.updatetorelease;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.core.maven.*;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiColor.DEFAULT;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
@ImportResource({
    "classpath:beans.xml",
    "classpath:project-version-updaters.xml",
    "classpath:project-parent-updaters.xml"
})
public class UpdateToReleaseCommand implements ApplicationRunner {
    private static final String VERSION_CONFIG_FILE = "rconfig.yaml";
    private static final String BACKUP_EXTENSION = "updatetorelease";

    @Autowired
    private List<VersionUpdate> updaters;

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UpdateToReleaseCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Updating version information to release version", DEFAULT));

        File versionSpecification = new File(VERSION_CONFIG_FILE);
        var versionConfig = new Yaml();

        try (var fileReader = new FileReader(versionSpecification)) {
            var releaseConfigs = versionConfig.<List<ReleaseConfig>>load(fileReader);

            updaters.forEach(updater -> {
                var releaseConfigOpt = releaseConfigs.stream().filter(x -> x.id.equals(updater.getId())).findFirst();
                var rc = releaseConfigOpt.orElseThrow(() -> new RTException("Unable to find version information for " +
                                                                            updater.getId() + " in " +
                                                                            VERSION_CONFIG_FILE));

                updater.setNextVersion(rc.releaseVersion);
            });

            Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

            projects.forEach(this::setReleaseVersions);
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to update Maven projects to next release version.");
        }
    }

    private void setReleaseVersions(ProjectRepository projectRepository) throws RTException {
        var backuper = new POMFileBackuper(BACKUP_EXTENSION);

        String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                       "About to update version information of ",
                                       BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                       projectRepository.getName(), ", ",
                                       AnsiColor.BRIGHT_YELLOW, NORMAL,
                                       "to the next release.", DEFAULT);
        System.out.println(s);
        String directory = projectRepository.getHumanName();
        backuper.makeBackUpOfPom(directory);

        VersionSetter versionSetter = new VersionSetter();
        versionSetter.set(directory, updaters);
    }
}
