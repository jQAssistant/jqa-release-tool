package com.buschmais.jqassistant.release.updatetonextdevversion;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.core.maven.*;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;

import static org.springframework.boot.ansi.AnsiColor.BRIGHT_GREEN;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_YELLOW;
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
public class UpdateToNextDevVersionCommand implements ApplicationRunner {
    private static String BACKUP_EXTENSION = "updatetonextdevversion";
    private static final String VERSION_CONFIG_FILE = "rconfig.yaml";

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
        var app = new SpringApplication(UpdateToNextDevVersionCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Update version information to development version", DEFAULT));
        var configFile = new File(VERSION_CONFIG_FILE);

        try (var fileReader = new FileReader(configFile)) {
            var versionConfig = new Yaml();
            var releaseConfigs = versionConfig.<List<ReleaseConfig>>load(fileReader);

            updaters.forEach(updater -> {
                var releaseConfigOpt = releaseConfigs.stream().filter(x -> x.id.equals(updater.getId())).findFirst();
                var rc = releaseConfigOpt.orElseThrow(() -> new RTException("Unable to find version information for " +
                                                                            updater.getId() + " in " +
                                                                            VERSION_CONFIG_FILE));

                updater.setNextVersion(rc.nextVersion);
            });

            var projects = getRepositorySrv().getProjectRepositories();

            projects.forEach(this::setReleaseVersions);
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to update Maven projects to development release version.");
        }
    }

    private void setReleaseVersions(ProjectRepository projectRepository) {
        var backuper = new POMFileBackuper(BACKUP_EXTENSION);

        var s = AnsiOutput.toString(BRIGHT_YELLOW, "About to update version information of '",
                                    BOLD, BRIGHT_YELLOW, projectRepository.getName(),
                                    NORMAL, BRIGHT_YELLOW, "' to the next dev version.",
                                    DEFAULT);
        System.out.println(s);

        var directory = projectRepository.getHumanName();
        backuper.makeBackUpOfPom(directory);

        var versionSetter = new VersionSetter();
        versionSetter.set(directory, updaters);
    }
}
