package com.buschmais.jqassistant.release.updatetonextdevversion;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.core.maven.*;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Set;

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
public class UpdateToNextDevVersionCommand implements CommandLineRunner {
    private static String BACKUP_EXTENSION = "updatetonextdevversion";

    @Autowired
    List<VersionUpdate> updaters;

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UpdateToNextDevVersionCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        // todo extract this in class for reuse
        File config = new File("/Users/plexus/jqa-rel-tools/rconfig.yaml");

        FileReader r = new FileReader(config);
        Yaml y = new Yaml();
        var load = y.<List<ReleaseConfig>>load(r);


        updaters.forEach(updater -> {
            var rco = load.stream().filter(x -> x.id.equals(updater.getId())).findFirst();
            var rc = rco.orElseThrow();

            updater.setNextVersion(rc.nextVersion);
        });

        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        projects.forEach(this::setReleaseVersions);

    }

    private void setReleaseVersions(ProjectRepository projectRepository) {
        var backuper = new POMFileBackuper(BACKUP_EXTENSION);

        String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                       "About to update version information of ",
                                       BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                       projectRepository.getName(),
                                       NORMAL, AnsiColor.BRIGHT_YELLOW,
                                       "' to the next dev version.", AnsiColor.DEFAULT);
        System.out.println(s);
        String directory = projectRepository.getHumanName();
        File original = backuper.makeBackUpOfPom(directory);

        VersionSetter versionSetter = new VersionSetter();
        versionSetter.set(directory, updaters);
    }
}
