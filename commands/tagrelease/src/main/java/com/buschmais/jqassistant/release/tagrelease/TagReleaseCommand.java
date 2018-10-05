package com.buschmais.jqassistant.release.tagrelease;

import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class TagReleaseCommand implements ApplicationRunner {
    private static final String VERSION_CONFIG_FILE = "rconfig.yaml";

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(TagReleaseCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Tagging all repositories", DEFAULT));
        var configFile = new File(VERSION_CONFIG_FILE);

        try (var fileReader = new FileReader(configFile)) {
            var versionConfig = new Yaml();
            var releaseConfigs = versionConfig.<List<ReleaseConfig>>load(fileReader);

            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                var path = projectRepository.getHumanName() + "/pom.xml";
                var fis = new FileInputStream(path);
                var mavenReader = new MavenXpp3Reader();
                var model = mavenReader.read(fis);
                var line = format("%s:%s", model.getGroupId(), model.getArtifactId());
                var releaseConfigOpt = releaseConfigs.stream().filter(rc -> rc.id.equals(line)).findFirst();
                var releaseConfig = releaseConfigOpt.orElseThrow(() -> new RTException("Unable to find version information for " +
                                                                                       line + " in " + VERSION_CONFIG_FILE));
                var tagName = "REL-" + releaseConfig.releaseVersion;
                var git = Git.open(new File(projectRepository.getHumanName()));

                git.tag()
                   .setName(tagName).setMessage("Release of " + releaseConfig.name + " " + releaseConfig.releaseVersion)
                   .call();

                var msg = AnsiOutput.toString(BRIGHT_YELLOW, "Tagged ", BOLD, BRIGHT_YELLOW, "'",
                                              projectRepository.getName(),
                                              NORMAL, BRIGHT_YELLOW, " with tag ", BOLD, BRIGHT_YELLOW, "'",
                                              tagName, NORMAL, "'", DEFAULT);

                System.out.println(msg);
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to tag all projects");
        }
    }
}
